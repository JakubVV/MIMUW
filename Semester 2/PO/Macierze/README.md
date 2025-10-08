# Matrix & Array Slices
**Object-Oriented Programming, Computer Science — Version 1.00**

---

## Overview

Design and implement classes representing **arrays of real numbers (`double`)**.  
We focus on arrays of dimensions:

- **0 (scalar)**  
- **1 (vector)** — horizontal or vertical  
- **2 (matrix)** — rectangular (not necessarily square)

Vectors and matrices **cannot be empty**.

Objects of these classes should provide typical arithmetic operations:

- **Addition**: `sum()` → returns new result, `add()` → modifies the object
- **Multiplication**: `product()` → returns new result, `multiply()` → modifies the object
- **Negation**: `negate()` → returns new object, `negateInPlace()` → modifies the object

**Modification rules**:  
In-place operations are allowed **only if the first argument has the same or larger dimension than the second** and has a compatible shape for the result.

---

## Operations Between Different Dimensions

1. **Scalar + Array** → elementwise operation on all elements of the array.
2. **Matrix + Vector** → depends on order and vector orientation:
   - Horizontal or vertical vector must match number of **columns or rows** of the matrix.  
   - Operation is applied **row-by-row** or **column-by-column**.
3. **Vector + Vector** → only if **same orientation** and **same length**:
   - Addition → elementwise sum (result: vector)
   - Multiplication:
     - Same orientation → **dot product** (result: scalar)  
     - Different orientation → **cross product** (result: matrix)

---

## Assignment (`assign`)

- Assign one array to another using `assign()`:
  - Argument must have **same or smaller dimension**.
  - Assignment rules:
    - Scalar → assign to all elements.
    - Vector → if assigning to matrix, elements copied row-wise or column-wise.
    - Dimensions must match appropriately:
      - Scalar → always valid
      - Same dimensions → shapes must match (vector orientation can change)
      - Vector → matrix rows/columns must match vector length

---

## Indexing

- **Read**: `get(...)`
- **Write**: `set(...)`
- Scalars → no indices  
- Vectors → one index  
- Matrices → two indices  

Also implement **varargs-style indexing**:
```java
public void mm(int... indices) {
    for (int i = 0; i < indices.length; i++) {
        System.out.print(indices[i] + " ");
    }
}
````

* Can be called with zero, one, two, etc. indices.

---

## Slices

* A **slice** of an array returns a **sub-array** of the same or smaller shape.
* Operations on slices **affect the original array** for in-place methods.
* Dimensions of the slice are the same as the original array.
* Slice arguments:

  * **Scalar** → one number
  * **Vector** → two numbers
  * **Matrix** → four numbers (rowStart, rowEnd, colStart, colEnd)
* Slice indexing is **zero-based** internally (not original array indices).
* Slices of slices are allowed.
* Arguments must be validated.

---

## Additional Methods

* `int dimension()` → returns 0, 1, or 2
* `int numberOfElements()` → total number of elements
* `int[] shape` → array of lengths per dimension

  * Vector → single number
  * Matrix → `[rows, columns]`
* `toString()` → textual representation, row formatting, orientation info
* `copy()` → deep copy
* `transpose()` → transposes:

  * Scalar → no effect
  * Vector → flips orientation
  * Matrix → transposes rows/columns

---

## Exceptions

* Use **custom checked exceptions**.
* Guidelines:

  * **Logical errors** → handle internally, print context, terminate program (`System.exit()` or assertion)
  * **Invalid argument values** → throw exception externally

---

## Method Overriding and Overloading

* **Overloading**: different method signatures (compile-time resolution)
* **Overriding**: subclass changes method implementation (runtime polymorphism)
* Example:

  * `assign(Scalar s)` overridden differently in `Scalar` vs `Vector`

---

## Task Requirements

1. Design and implement classes for **scalars, vectors, matrices** with above functionality.
2. Implement code demonstrating operations (`Tests` section).
3. Submit:

   * Java source files (`.java`) organized into **packages** (`src/`)
   * Compressed as `src.zip`
   * **Do not include other files**

**Important:** Keep the method and class names given in the task specification (task is 
originally written in Polish, class names remain original).


