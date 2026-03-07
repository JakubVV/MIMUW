
---

## Datalog and Its Semantics

Datalog is a logic-based language used to answer queries about relationships between constants, based on a fixed set of rules. Each program operates on a **finite set of constants** and a set of **predicates** (relation names with fixed arity).

### Statements, Rules, and Queries

* An **atom** has the form `p(t1, ..., tn)`, where terms are constants or variables.
* A **statement** is an atom containing only constants.
* A **rule** has the form
  `head :- body1, ..., bodyk.`
  where the head and body elements are atoms.
* Rules with an empty body represent **facts**.
* **Queries** are ground statements (no variables) whose derivability we want to check.

### Derivability Semantics

A statement `p(c1, ..., cn)` is **derivable** if there exists:

1. A rule whose head predicate is `p`,
2. A substitution of variables with constants such that the rule’s head becomes exactly `p(c1, ..., cn)`,
3. And all atoms in the rule’s body (after substitution) are themselves derivable.

This recursive definition is equivalent to the existence of a **finite derivation tree**, where each node is justified by a rule applied to its children, and leaves correspond to facts.

If no such finite derivation exists, the statement is **non-derivable**. Cyclic rule dependencies alone do not justify derivability.

### Handling Cycles

During top-down evaluation, cycles may occur (e.g. a rule requiring the same statement it tries to derive). Such derivation paths are rejected. A statement is concluded non-derivable only if **all** possible derivations fail.

### Oracle Extension

The language is extended with an **oracle** that provides truth values for selected predicates:

* `isCalculatable(predicate)` determines whether a predicate is handled by the oracle.
* For calculatable predicates, derivability is decided solely by `calculate(statement)`.
* Rules whose head predicate is calculatable are ignored.
* Calculatable predicates appear only as leaves in derivation trees.

### Query Evaluation Algorithm

The reference engine (`SimpleDeriver`) uses a **top-down, depth-first search**:

* For a statement, it tries all matching rules and substitutions.
* Each body atom is derived recursively.
* If one derivation succeeds, the statement is derivable.
* A set of “in-progress” statements is maintained to detect cycles.
* Statements for which all derivations fail are marked as non-derivable.

### Task

The goal is to implement `ParallelDeriver`, a parallel version of the derivation engine that implements `AbstractDeriver` and produces **exactly the same query results** as `SimpleDeriver`.

---
