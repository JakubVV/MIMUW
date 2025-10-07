# Sokoban – Program Specification

## Introduction

**Sokoban** is a single-player logic puzzle played on a two-dimensional grid of square cells.

Some cells are **empty**, while others contain **walls** or **boxes**.  
A certain number of cells are marked as **target positions** — a target cell may be empty or may contain a box.

A **player character** occupies one of the cells on the board and can move **vertically or horizontally** to an adjacent cell.

Two cells are considered **adjacent** to the cell at row *w* and column *k* if they are located at:

- row *w − 1*, column *k*  
- row *w + 1*, column *k*  
- row *w*, column *k − 1*  
- row *w*, column *k + 1*

The player can move onto a cell if it is **free** or if it contains a **box** that can be **pushed**.

A box can be pushed **only if** the cell directly behind it (in the direction of movement) is **free**.  
The player cannot move or push a box **outside the board**, nor can they **push more than one box** at a time.

The **goal** of the game is to place all boxes onto the **target cells**.

---

## Task

Write a program that allows playing **Sokoban**.

The program reads the **initial board state** and then processes a **sequence of commands**.

The recognized commands are:

- Printing the **current board state**
- **Pushing** a box
- **Undoing** a previously executed push

Unlike typical Sokoban implementations, the user **does not need to move the player manually** between free cells.  
The program automatically determines how to move the player to the correct position from which a push can be performed.

---

## Input Format

The input consists of:

1. A description of the **initial board state**, followed by  
2. A **non-empty sequence of commands**, ending with a **period (`.`)**.

Everything after the final period is ignored.

The **first command** is always a **print command** (newline).

### Board Description

The board is described using **non-empty lines**, each containing single-character symbols representing cell states:

| Symbol | Meaning |
|:--|:--|
| `-` | Empty cell (not a target) |
| `+` | Target cell (empty) |
| `#` | Wall |
| `@` | Player on a non-target cell |
| `*` | Player on a target cell |
| `[a..z]` | Box (named by lowercase letter) on a non-target cell |
| `[A..Z]` | Box (named by corresponding lowercase letter) on a target cell |

A valid board description must contain **exactly one player**, and each box (letter) appears **only once**.

### Commands

| Command | Description |
|:--|:--|
| **newline (`\n`)** | Print the current board state |
| **[a..z][2/4/6/8]** | Push the box named by the lowercase letter in a specific direction |
| **0** | Undo the last successfully executed (and not yet undone) push |

#### Push Directions

| Digit | Direction |
|:--|:--|
| `2` | Down |
| `8` | Up |
| `4` | Left |
| `6` | Right |

The player automatically moves along a path of **free cells** to the correct position for the push.  
The path **cannot** include walls or boxes.

If the push **cannot** be successfully executed (e.g., no valid path or the box cannot be pushed), the board state **remains unchanged**.

#### Undo Command (`0`)

The undo command reverts the **last successfully executed** push (that has not yet been undone).  
The player returns to the position they occupied **before** that push.

If there are **no pushes to undo**, the command has **no effect**.

---

## Output Format

The output consists of the **board states** printed in response to print commands.

Each printed board state has the **same format** as the input board description.

That is, the output uses the same symbols (`-`, `+`, `#`, `@`, `*`, `[a..z]`, `[A..Z]`) and preserves the layout of the board.
