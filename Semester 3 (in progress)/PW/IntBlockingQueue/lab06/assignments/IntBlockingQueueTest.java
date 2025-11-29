package lab06.assignments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class IntBlockingQueueTest {
    private static boolean testQueue() {
        int capacity = 3;
        IntBlockingQueue blockingQueue = new IntBlockingQueue(capacity);
        IntBlockingQueue testingQueue = new IntBlockingQueue(2);

        List<Thread> threads = new ArrayList<>();

        threads.add(new Thread(() -> {
            try {
                assertThat(blockingQueue.getSize() == 0, "Expected empty queue (pre).");
                for (int i = 0; i < capacity + 1; i++) {
                    blockingQueue.put('A' + i);
                }
                // The last put was above capacity,
                // so it should wait until the other thread starts to take().
                // By then, the other thread should have put 'X' on testingQueue.
                testingQueue.put('Y');
                int s = testingQueue.getSize();
                assertThat(s == 2, String.format("Expected size 2, got: %d", s));
                int character = testingQueue.take();
                assertThat(character == 'X', String.format("Expected 'X', got: %c", character));

                testingQueue.put('Z');

                // Put the very last element to blockingQueue.
                blockingQueue.put('A' + capacity + 1);
            } catch (InterruptedException | IntBlockingQueue.ShutdownException e) {
                throw new RuntimeException(e);
            }
        }));

        threads.add(new Thread(() -> {
            try {
                Thread.sleep(50);
                testingQueue.put('X');
                for (int i = 0; i < capacity + 2; i++) {
                    blockingQueue.take();
                }
                // The last take above should wait until the other thread puts its very last
                // element to blockingQueue.
                // By then, it should have taken 'X' from testingQueue, leaving 'Y' and 'Z'.
                int character = testingQueue.take();
                assertThat(character == 'Y', String.format("Expected 'Y', got: %c", character));
                character = testingQueue.take();
                assertThat(character == 'Z', String.format("Expected 'Z', got: %c", character));
                assertThat(testingQueue.getSize() == 0, "Expected empty queue (post).");
            } catch (InterruptedException | IntBlockingQueue.ShutdownException e) {
                throw new RuntimeException(e);
            }
        }));

        return testThreads(Duration.ofMillis(300), threads);
    }

    private static boolean testMultipleProducersConsumers() {
        int capacity = 2;
        int producersConsumersCount = 100;
        IntBlockingQueue queue = new IntBlockingQueue(capacity);

        List<Thread> threads = new ArrayList<>();

        Runnable producer = () -> {
            try {
                for (int i = 0; i < capacity + 1; i++) {
                    queue.put(i);
                }
            } catch (InterruptedException | IntBlockingQueue.ShutdownException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable consumer = () -> {
            try {
                for (int i = 0; i < capacity + 1; i++) {
                    queue.take();
                }
            } catch (InterruptedException | IntBlockingQueue.ShutdownException e) {
                throw new RuntimeException(e);
            }
        };

        for (int i = 0; i < producersConsumersCount; i++) {
            threads.add(new Thread(producer));
            threads.add(new Thread(consumer));
        }

        return testThreads(Duration.ofMillis(500), threads);
    }

    private static boolean testShutdown() {
        int capacity = 2;
        int producersConsumersCount = 100;
        IntBlockingQueue queue = new IntBlockingQueue(capacity);
        LinkedBlockingQueue<Integer> takeCounts = new LinkedBlockingQueue<Integer>();
        LinkedBlockingQueue<Integer> putCounts = new LinkedBlockingQueue<Integer>();

        List<Thread> threads = new ArrayList<>();

        Runnable shutdowner = () -> {
            try {
                queue.shutdown();

                int totalPuts = 0;
                int totalTakes = 0;
                for (int i = 0; i < producersConsumersCount; i++) {
                    totalPuts += putCounts.take();
                    totalTakes += takeCounts.take();
                }
                System.out.println("Total puts: " + totalPuts + ", total takes: " + totalTakes);
                assertThat(totalPuts == totalTakes,
                        String.format("Expected totalPuts == totalTakes, got: %d + %d == %d",
                                totalPuts, queue.getSize(), totalTakes));

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable producer = () -> {
            int successfulPuts = 0;
            int failedPuts = 0;
            try {
                Thread.sleep(1);
                for (int i = 0; i < capacity + 1; i++) {
                    try {
                        queue.put(i);
                        successfulPuts++;
                    } catch (IntBlockingQueue.ShutdownException e) {
                        // Expected exception
                        failedPuts++;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            assertThat(successfulPuts + failedPuts == capacity + 1,
                    String.format("Expected %d puts (successful + failed), got: %d + %d",
                            capacity + 1, successfulPuts, failedPuts));

            putCounts.add(successfulPuts);
        };

        Runnable consumer = () -> {
            int successfulTakes = 0;
            int failedTakes = 0;
            try {
                Thread.sleep(1);
                for (int i = 0; i < capacity + 1; i++) {
                    try {
                        queue.take();
                        successfulTakes++;
                    } catch (IntBlockingQueue.ShutdownException e) {
                        // Expected exception
                        failedTakes++;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            assertThat(successfulTakes + failedTakes == capacity + 1,
                    String.format("Expected %d takes (successful + failed), got: %d + %d",
                            capacity + 1, successfulTakes, failedTakes));

            takeCounts.add(successfulTakes);
        };

        for (int i = 0; i < producersConsumersCount; i++) {
            threads.add(new Thread(producer));
            threads.add(new Thread(consumer));
        }

        threads.add(new Thread(shutdowner));

        return testThreads(Duration.ofMillis(500), threads);
    }

    private static void assertThat(boolean predicate, String message) {
        // Not using built-in `assert`, because it's disabled by default (even in debug)
        // and tedious to enable (pass the -enableassertions or -ea flag to the JVM).
        if (!predicate) {
            throw new AssertionError(message);
        }
    }

    // Start threads and check whether they finish in time and without exceptions.
    private static boolean testThreads(Duration timeout, List<Thread> threads) {
        AtomicBoolean ok = new AtomicBoolean(true);

        for (Thread t : threads) {
            t.setUncaughtExceptionHandler((thread, exception) -> {
                ok.set(false);
                exception.printStackTrace();
            });
        }

        for (Thread t : threads) {
            t.start();
        }

        try {
            Thread.sleep(timeout.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Thread t : threads) {
            if (t.isAlive()) {
                System.out.println("Timeout on thread: " + t);
                t.interrupt();
                ok.set(false);
            }
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (ok.get()) {
            System.out.println("OK");
            return true;
        } else {
            System.out.println("FAIL");
            return false;
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println("== Test run " + (i + 1) + " ==");
            if (!testQueue())
                break;
            if (!testMultipleProducersConsumers())
                break;
            if (!testShutdown())
                break;
            // if (!testRendezvous())
            // break;
        }
    }

}
