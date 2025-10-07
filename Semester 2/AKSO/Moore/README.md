# Moore Automata — Task Specification

## Description

The task is to implement a **C** dynamically loaded library that simulates **Moore automata**.

A Moore automaton is a type of deterministic finite automaton used in synchronous digital circuits.

A Moore automaton is represented as an ordered sextuple:

⟨X, Y, Q, t, y, q⟩

where:

- **X** — set of input signal values,
- **Y** — set of output signal values,
- **Q** — set of internal states,
- **t : X × Q → Q** — transition function,
- **y : Q → Y** — output function,
- **q ∈ Q** — initial state.

We consider only binary automata with:
- `n` one-bit input signals,
- `m` one-bit output signals,
- `s` state bits.

Formally:

```

X = {0,1}ⁿ
Y = {0,1}ᵐ
Q = {0,1}ˢ

````

At each step, the function `t` computes a new automaton state based on input signals and the current state.  
The function `y` computes the output signals based on the current state.

---

## Library Interface

The library interface is in `ma.h`. Additional details can be inferred from the example usage `ma_example.c`.

### Bit Representation

Bit sequences and signal values are stored in arrays of type `uint64_t`.  
Each element stores up to 64 consecutive bits starting from the least significant bit.

---

## Type Definitions

### Automaton structure

```c
typedef struct moore moore_t;
````

### Transition function

```c
typedef void (*transition_function_t)(uint64_t *next_state, uint64_t const *input, uint64_t const *state, size_t n, size_t s);
```

**Parameters:**

* `next_state` — new state of the automaton,
* `input` — input signals,
* `state` — current state,
* `n` — number of input signals,
* `s` — number of state bits.

### Output function

```c
typedef void (*output_function_t)(uint64_t *output, uint64_t const *state, size_t m, size_t s);
```

**Parameters:**

* `output` — output signals,
* `state` — current state,
* `m` — number of output signals,
* `s` — number of state bits.

---

## Library Functions

### `ma_create_full`

Creates a new Moore automaton.

```c
moore_t *ma_create_full(size_t n, size_t m, size_t s, transition_function_t t, output_function_t y, uint64_t const *q);
```

**Parameters:**

* `n`, `m`, `s` — number of inputs, outputs, and state bits,
* `t` — transition function,
* `y` — output function,
* `q` — initial state.

**Returns:**

* pointer to the automaton or `NULL` on error (`errno = EINVAL` or `ENOMEM`).

---

### `ma_create_simple`

Creates a simplified automaton where outputs = state and output function is the identity.

```c
moore_t *ma_create_simple(size_t n, size_t s, transition_function_t t);
```

**Returns:**

* pointer to the new automaton or `NULL` on error.

---

### `ma_delete`

Deletes an automaton and frees its memory.

```c
void ma_delete(moore_t *a);
```

---

### `ma_connect`

Connects outputs of one automaton to inputs of another.

```c
int ma_connect(moore_t *a_in, size_t in, moore_t *a_out, size_t out, size_t num);
```

**Returns:**

* `0` — success,
* `-1` — error (`errno = EINVAL` or `ENOMEM`).

---

### `ma_disconnect`

Disconnects inputs of an automaton.

```c
int ma_disconnect(moore_t *a_in, size_t in, size_t num);
```

---

### `ma_set_input`

Sets input signals for unconnected inputs.

```c
int ma_set_input(moore_t *a, uint64_t const *input);
```

---

### `ma_set_state`

Sets the state of an automaton.

```c
int ma_set_state(moore_t *a, uint64_t const *state);
```

---

### `ma_get_output`

Returns a pointer to the array of bits representing the current output.

```c
uint64_t const *ma_get_output(moore_t const *a);
```

---

### `ma_step`

Performs one computation step for a set of automata (all run in parallel and synchronously).

```c
int ma_step(moore_t *at[], size_t num);
```

**Returns:**

* `0` — success,
* `-1` — error (`errno = EINVAL` or `ENOMEM`).

---

## Functional Requirements

* Inputs can be set via `ma_set_input` or `ma_connect`.
* Connections must be correctly managed when deleting automata.
* The library must handle allocation errors without losing memory.

---

## Formal Requirements

Files to provide:

* `ma.c` (implementation),
* `ma.h` (header),
* `Makefile` or `makefile`.

### Compilation

Using `gcc` with options:

```
-Wall -Wextra -Wno-implicit-fallthrough -std=gnu17 -fPIC -O2
```

### Linking

```
-shared -Wl,--wrap=malloc -Wl,--wrap=calloc -Wl,--wrap=realloc -Wl,--wrap=reallocarray -Wl,--wrap=free -Wl,--wrap=strdup -Wl,--wrap=strndup
```

`--wrap` options allow testing memory error resilience (see `memory_tests.c`).

### Makefile

Compile library:

```
make libma.so
```

Clean files:

```
make clean
```

Include `.PHONY` pseudo-targets.

---

## Testing

Correctness will be checked using **valgrind** — implementation must not leak memory or leave data structures in an inconsistent state.

---
