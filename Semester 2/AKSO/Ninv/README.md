# Inverse Function — `ninv`

## Description

For a given **n-bit** integer `x` (where `x > 1`), we want to determine a positive **n-bit** integer `y` such that:

```

x * y ≤ 2ⁿ < x * (y + 1)

````

In other words, `y` is the **integer part of (2ⁿ / x)**, truncated to fit within `n` bits.

---

## Task

Implement in **assembly language** a function callable from **C** with the following declaration:

```c
void ninv(uint64_t *y, uint64_t const *x, unsigned n);
````

---

## Parameters

| Parameter | Type               | Description                                                                                                  |
| --------- | ------------------ | ------------------------------------------------------------------------------------------------------------ |
| `y`       | `uint64_t *`       | Pointer to the memory location where the result (number `y`) will be stored.                                 |
| `x`       | `uint64_t const *` | Pointer to the memory location containing the binary representation of number `x`.                           |
| `n`       | `unsigned`         | The number of bits of both `x` and `y`. It is always a multiple of 64 and within the range **[64, 256000]**. |

---

## Representation of Numbers

* Both `x` and `y` are represented in **natural binary form** (standard binary, little-endian order).
* Each value is stored as a sequence of **64-bit words (`uint64_t`)**.
* The **least significant word** comes first (little-endian ordering).
* There are exactly `n / 64` words for each number.

Example for `n = 128`:

```
x = [x_low, x_high]
y = [y_low, y_high]
```

Where:

* `x_low` and `y_low` are the least significant 64 bits.
* `x_high` and `y_high` are the most significant 64 bits.

---

## Requirements

1. The function **must not modify** the memory pointed to by `x`.
2. The function **must not assume any initial value** in the memory pointed to by `y`.
3. The function **must not check** the correctness of the arguments (no validation of `x`, `y`, or `n`).
4. The implementation must handle `n` up to **256,000 bits** (≈ 32 KB of 64-bit words).

---
