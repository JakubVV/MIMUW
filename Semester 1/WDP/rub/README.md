# Rubik's Cube – Program Specification

## Introduction

A **Rubik’s Cube** is a mechanical puzzle composed of movable cubic blocks.

The faces of the cube are divided into colored squares belonging to individual blocks.  
In the **solved state**, all squares on each face share the same color — a different color for each face.

Each block belongs to **three perpendicular layers** of the cube. The blocks can be moved by **rotating layers** by multiples of 90 degrees.

The goal of the puzzle is to transform a **randomly scrambled cube** back into its **solved state**.

The most common version is a **3×3×3 cube**, but larger and smaller versions exist.

---

## Task

Write a program that executes a **sequence of commands** that rotate the layers of an **N×N×N cube**, where **N** is a positive integer (of type `int`).  
The program starts with a **solved cube** and, upon request, prints its **current state**.

The constant **N**, defining the cube’s size, should be defined using the following preprocessor directives:

```c
#ifndef N
#define N 5
#endif
```

This sets `N = 5` as the default value.  
A different value can be chosen during compilation using the option `-DN=value`.

---

## Input Format

The program input consists of a **sequence of commands** that rotate layers or print the cube’s current state, **ending with a period (`.`)**.  
All input after the period is ignored.

### Printing Command
A **newline character (`\n`)** represents a print command — it outputs the current cube state.

### Rotation Command
A **rotation command** consists of three parts:

1. **Number of layers**  
2. **Face of the cube**  
3. **Rotation angle**  

If the number of layers is **omitted**, it is assumed to be `1`.  
If present, it is a **positive integer** not exceeding `N`.

#### Face Letters

| Letter | Face | Meaning |
|:--|:--|:--|
| `u` | up | top face |
| `l` | left | left face |
| `f` | front | front face |
| `r` | right | right face |
| `b` | back | back face |
| `d` | down | bottom face |

#### Rotation Angles

| Symbol | Meaning | Degrees |
|:--|:--|:--|
| *(empty)* | clockwise | 90° |
| `'` (apostrophe) | counterclockwise | -90° |
| `"` (quote) | half turn | 180° |

Executing a rotation command with **W layers**, **face S**, and **angle K** rotates **W layers** (as seen from face S) by **angle K**, **clockwise**.

---

## Input Grammar

```
Data → CommandSequence Period
CommandSequence → ε | Command CommandSequence
Command → Rotation | Print
Rotation → LayerCount Face Angle
Print → NewLine
Face → Letter
LayerCount → One | Many
One → ε
Many → Number
Number → Positive | Number Digit
Angle → Quarter | MinusQuarter | Half
Quarter → ε
MinusQuarter → Apostrophe
Half → Quote
Letter → 'u' | 'l' | 'f' | 'r' | 'b' | 'd'
Positive → '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
Digit → '0' | Positive
Apostrophe → '\''
Quote → '"'
NewLine → '\n'
Period → '.'
```

**Terminal symbols:**  
`'u'`, `'l'`, `'f'`, `'r'`, `'b'`, `'d'`, `'0'`–`'9'`, `'.'`, `'"'` represent literal characters.  
`'\''` is an apostrophe, and `'\n'` is a newline character.

---

## Output Format

The **program output** is the result of executing all **print commands**, showing the current cube state.

### Cube State Representation

Each printed cube state consists of:
- Digits **0–5**, representing colors of face squares
- Spaces `' '` and vertical bars `'|'`

In the **solved cube**, faces have the following colors:

| Face | Color |
|:--|:--|
| Up | 0 |
| Left | 1 |
| Front | 2 |
| Right | 3 |
| Back | 4 |
| Down | 5 |

### Layout of the Printed Cube

```
  u
l|f|r|b
  d
```

Where:
- `u` = up face  
- `l` = left face  
- `f` = front face  
- `r` = right face  
- `b` = back face  
- `d` = down face  

---

## Structure of the Output

For a cube of size **N**, the printed state contains **3×N + 1 lines**:

1. The **first line** is empty.  
2. The **next N lines** describe the **upper face**.  
3. The **following N lines** describe the **left**, **front**, **right**, and **back** faces.  
4. The **last N lines** describe the **bottom face**.

### Formatting Rules

- Each line describing the **upper** and **lower** faces starts with **N + 1 spaces**, followed by **N digits** (the colors).  
- Lines describing the **left**, **front**, **right**, and **back** faces contain:  
  `N digits | N digits | N digits | N digits`

The digits representing the colors are ordered as they would appear when the entire cube is rotated in 90° increments around the axis passing through the **up** and **down** faces.

For the **upper** and **lower** faces, the order corresponds to rotations around the axis passing through the **left** and **right** faces.
