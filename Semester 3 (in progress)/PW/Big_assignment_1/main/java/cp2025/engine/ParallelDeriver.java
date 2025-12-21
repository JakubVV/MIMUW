package cp2025.engine;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import cp2025.engine.Datalog.*;


public class ParallelDeriver implements AbstractDeriver {
    private final int numWorkers;

    public ParallelDeriver(int numWorkers) { 
        this.numWorkers = numWorkers; 
    }

    @Override
    public Map<Atom, Boolean> derive(Program input, AbstractOracle oracle)
            throws InterruptedException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(oracle, "oracle");

        SharedState shared = new SharedState(input, oracle);

        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, numWorkers));
        List<Future<Map.Entry<Atom, Boolean>>> futures = new ArrayList<>();

        for (Atom query : input.queries()) {
            futures.add(pool.submit(() -> {
                Worker worker = new Worker(shared);
                DerivationResult res = worker.deriveStatement(query);
                return Map.entry(query, res.derivable);
            }));
        }

        Map<Atom, Boolean> results = new LinkedHashMap<>();
        try {
            for (Future<Map.Entry<Atom, Boolean>> f : futures) {
                Map.Entry<Atom, Boolean> e = f.get();
                results.put(e.getKey(), e.getValue());
            }
        } catch (InterruptedException ie) {
            // Derive() was interrupted — signal shutdown and propagate.
            shared.requestShutdown();
            pool.shutdownNow();
            throw ie;
        } catch (ExecutionException ee) {
            // If worker propagated InterruptedException, unwrap to InterruptedException.
            Throwable cause = ee.getCause();
            if (cause instanceof InterruptedException ie) {
                shared.requestShutdown();
                pool.shutdownNow();
                throw ie;
            }
            // Ensure cleanup.
            shared.requestShutdown();
            pool.shutdownNow();
            // Re-throw as unchecked to indicate failure context.
            throw new RuntimeException(cause);
        } finally {
            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        return results;
    }

    // Shared state across all worker threads handling queries for a single derive() call. 
    private static final class SharedState {
        final Program input;
        final AbstractOracle oracle;

        final Map<Predicate, List<Rule>> predicateToRules;

        // Known derivability results.
        final ConcurrentHashMap<Atom, Boolean> knownStatements = new ConcurrentHashMap<>();

        // Threads currently deriving a given statement (anywhere in their recursion).
        final ConcurrentHashMap<Atom, CopyOnWriteArraySet<WorkerContext>> derivingWaiters =
                new ConcurrentHashMap<>();

        // Threads currently executing oracle.calculate for a given statement.
        final ConcurrentHashMap<Atom, CopyOnWriteArraySet<WorkerContext>> calculatingWaiters =
                new ConcurrentHashMap<>();

        final AtomicBoolean shutdown = new AtomicBoolean(false);

        SharedState(Program input, AbstractOracle oracle) {
            this.input = input;
            this.oracle = oracle;
            this.predicateToRules = input.rules().stream().collect(
                    java.util.stream.Collectors.groupingBy(r -> r.head().predicate()));
        }

        void requestShutdown() { 
            shutdown.set(true); 
        }

        boolean isShutdown() { 
            return shutdown.get(); 
        }

        boolean isKnown(Atom a) { 
            return knownStatements.containsKey(a); 
        }

        Optional<Boolean> getKnown(Atom a) {
            Boolean v = knownStatements.get(a);
            return v == null ? Optional.empty() : Optional.of(v);
        }

        void registerDeriving(Atom a, WorkerContext ctx) {
            derivingWaiters.computeIfAbsent(a, k -> new CopyOnWriteArraySet<>()).add(ctx);
        }

        void unregisterDeriving(Atom a, WorkerContext ctx) {
            CopyOnWriteArraySet<WorkerContext> set = derivingWaiters.get(a);
            if (set != null) set.remove(ctx);
        }

        void registerCalculating(Atom a, WorkerContext ctx) {
            calculatingWaiters.computeIfAbsent(a, k -> new CopyOnWriteArraySet<>()).add(ctx);
        }

        void unregisterCalculating(Atom a, WorkerContext ctx) {
            CopyOnWriteArraySet<WorkerContext> set = calculatingWaiters.get(a);
            if (set != null) set.remove(ctx);
        }

        // Announce a known result and notify interested workers.
        void announceKnown(Atom a, boolean value, WorkerContext source) {
            // Record known, do not overwrite existing to avoid flip-flop.
            knownStatements.putIfAbsent(a, value);

            // Notify deriving waiters (cooperative cancel via checking known map soon).
            CopyOnWriteArraySet<WorkerContext> waiters1 = derivingWaiters.get(a);
            if (waiters1 != null) {
                for (WorkerContext ctx : waiters1) {
                    if (ctx != source) ctx.markSuperseded(a);
                }
            }

            // Interrupt those that may be blocked in calculate.
            CopyOnWriteArraySet<WorkerContext> waiters2 = calculatingWaiters.get(a);
            if (waiters2 != null) {
                for (WorkerContext ctx : waiters2) {
                    if (ctx != source) {
                        ctx.markSuperseded(a);
                        ctx.thread().interrupt();
                    }
                }
            }
        }
    }

    // Per-thread context used for coordination and cancellation. 
    private static final class WorkerContext {
        private final Thread thread;
        // Track statements for which this worker was superseded.
        private final Set<Atom> superseded = ConcurrentHashMap.newKeySet();

        WorkerContext(Thread thread) { 
            this.thread = thread; 
        }

        Thread thread() { 
            return thread; 
        }

        void markSuperseded(Atom a) {
            superseded.add(a);
        }
    }

    // Result type mirroring SimpleDeriver.SimpleDeriverState.DerivationResult. 
    private record DerivationResult(boolean derivable, Set<Atom> failedStatements) {}

    // Worker that performs DFS derivation for a single query, reusing shared caches. 
    private static final class Worker {
        private final SharedState shared;
        private final WorkerContext ctx;
        private final Set<Atom> inProgress = new HashSet<>();

        Worker(SharedState shared) {
            this.shared = shared;
            this.ctx = new WorkerContext(Thread.currentThread());
        }

        DerivationResult deriveStatement(Atom goal) throws InterruptedException {
            // Respect shutdown promptly.
            if (shared.isShutdown()) throw new InterruptedException();

            // Fast path: use shared cache.
            Optional<Boolean> known = shared.getKnown(goal);
            if (known.isPresent()) return new DerivationResult(known.get(), Set.of());

            // If calculatable, let the oracle decide.
            if (shared.oracle.isCalculatable(goal.predicate())) {
                // Register interest before possibly blocking in calculate().
                shared.registerDeriving(goal, ctx);
                shared.registerCalculating(goal, ctx);
                try {
                    // Re-check if someone resolved it just now.
                    Optional<Boolean> preKnown = shared.getKnown(goal);
                    if (preKnown.isPresent()) return new DerivationResult(preKnown.get(), Set.of());

                    boolean result;
                    try {
                        result = shared.oracle.calculate(goal);
                    } catch (InterruptedException ie) {
                        if (shared.isShutdown()) throw ie; // Derive() cancelled.
                        // Otherwise interpret as superseded; return using known if available.
                        Optional<Boolean> k = shared.getKnown(goal);
                        if (k.isPresent()) return new DerivationResult(k.get(), Set.of());
                        // If not known yet, propagate interruption upwards as cooperative cancel.
                        throw ie;
                    }

                    // Record and announce result.
                    shared.announceKnown(goal, result, ctx);
                    return new DerivationResult(result, Set.of());
                } finally {
                    shared.unregisterCalculating(goal, ctx);
                    shared.unregisterDeriving(goal, ctx);
                }
            }

            // Non-calculatable: DFS using rules.
            if (inProgress.contains(goal)) {
                // Cycle: cannot derive without using in-progress statements.
                return new DerivationResult(false, Set.of(goal));
            }

            shared.registerDeriving(goal, ctx);
            inProgress.add(goal);
            try {
                DerivationResult res = deriveNewStatement(goal);

                if (res.derivable) {
                    shared.announceKnown(goal, true, ctx);
                } else {
                    // Only commit falses when at the top of recursion for this worker.
                    if (inProgress.size() == 1) {
                        for (Atom a : res.failedStatements) shared.announceKnown(a, false, ctx);
                    }
                }
                return res;
            } finally {
                inProgress.remove(goal);
                shared.unregisterDeriving(goal, ctx);
            }
        }

        private DerivationResult deriveNewStatement(Atom goal) throws InterruptedException {
            // If someone resolved it in the meantime, stop.
            Optional<Boolean> known = shared.getKnown(goal);
            if (known.isPresent()) return new DerivationResult(known.get(), Set.of());

            List<Rule> rules = shared.predicateToRules.get(goal.predicate());
            if (rules == null) return new DerivationResult(false, Set.of(goal));

            Set<Atom> failedStatements = new HashSet<>();

            for (Rule rule : rules) {
                if (shared.isShutdown()) throw new InterruptedException();

                // If current goal was superseded, short-circuit.
                if (shared.isKnown(goal)) return new DerivationResult(shared.knownStatements.get(goal), Set.of());

                Optional<List<Atom>> partiallyAssignedBody = Unifier.unify(rule, goal);
                if (partiallyAssignedBody.isEmpty()) continue;

                List<Variable> variables = Datalog.getVariables(partiallyAssignedBody.get());
                FunctionGenerator<Variable, Constant> iterator =
                        new FunctionGenerator<>(variables, shared.input.constants());
                for (Map<Variable, Constant> assignment : iterator) {
                    if (shared.isShutdown()) throw new InterruptedException();
                    if (shared.isKnown(goal)) return new DerivationResult(shared.knownStatements.get(goal), Set.of());

                    List<Atom> assignedBody = Unifier.applyAssignment(partiallyAssignedBody.get(), assignment);
                    DerivationResult result = deriveBody(assignedBody);
                    if (result.derivable) return new DerivationResult(true, Set.of());
                    failedStatements.addAll(result.failedStatements);
                }
            }

            failedStatements.add(goal);
            return new DerivationResult(false, failedStatements);
        }

        private DerivationResult deriveBody(List<Atom> body) throws InterruptedException {
            for (Atom statement : body) {
                if (shared.isShutdown()) throw new InterruptedException();
                DerivationResult result = deriveStatement(statement);
                if (!result.derivable) return new DerivationResult(false, result.failedStatements);
            }
            return new DerivationResult(true, Set.of());
        }
    }
}
