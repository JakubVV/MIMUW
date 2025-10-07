# SET – Game Simulation

## Introduction

**SET** is a real-time card game for any number of players. The game does not impose a turn order — players act simultaneously.

In **SET**, the deck consists of special cards. Each card has **four attributes**, and each attribute can take one of **three possible values**:

- **Number of shapes**: one, two, or three  
- **Color**: red, green, or purple  
- **Filling**: empty, striped, or solid  
- **Shape**: diamond, squiggle, or oval  

Since there are 3 × 3 × 3 × 3 = 81 possible combinations, the deck contains **81 unique cards**, each with a distinct combination of attributes.

A **set** is a group of three cards where, for each attribute, the values are either **all the same** or **all different** across the three cards.

### Example of a valid set

- Two red solid diamonds  
- Three purple solid squiggles  
- One green solid oval  

### Example of a non-set

- Three green empty squiggles  
- One red solid squiggle  
- Two green striped squiggles  

---

## Game Setup and Rules

At the start of the game, **12 cards** are laid face up on the table (typically arranged in three rows of four, though order doesn’t matter).  
For this task, we assume the cards are arranged in a **sequence** in the order they were drawn from the deck.  
We number positions in this sequence starting from **1**.

Players compete to find a **set** among the cards on the table.  
When a player spots a set, they call “SET!” to claim it, then point out the three cards.  

- If the cards form a valid set, the player removes them from the table.  
- If not, they incur a penalty (agreed upon before the game).  

After removing a set:

- If there are **fewer than 12 cards** on the table and **cards remain in the deck**, draw cards from the deck to refill the table back to 12.  
- If no sets are found by any player, **add three new cards** from the deck to the table.  

The game ends when:
- No sets are present on the table, **and**
- The deck is **empty**.

Each player’s score equals the number of sets they have collected.

---

## Task Description

Write a **program** that simulates the **endgame** of SET.

Players always choose the **“first” available set** on the table.  
A set consisting of cards at positions **x < y < z** precedes another set **x' < y' < z'** if:

- **x < x'**, or  
- **x = x' and y < y'**, or  
- **x = x' and y = y' and z < z'**

---

## Input Format

The input is a sequence of integers in the range **1111–3333**, with **no repetitions**.  
The total count of numbers is **a multiple of 3**.  
Numbers may be separated by spaces and line breaks.

Each number represents a card using **four digits (A B C D)**:

| Digit | Attribute | Values |
|:--|:--|:--|
| A | Number of shapes | 1 – one, 2 – two, 3 – three |
| B | Color | 1 – red, 2 – green, 3 – purple |
| C | Filling | 1 – empty, 2 – striped, 3 – solid |
| D | Shape | 1 – diamond, 2 – squiggle, 3 – oval |

The first **12 cards** form the **table**, or all cards if there are fewer than 12.  
The remaining cards form the **deck**, in the order they appear in the input.

---

## Output Format

The program should output:

1. The **initial table state**,  
2. Then a **sequence of plays (moves)** until the game ends,  
3. After each move, the **current state of the table**.

### Move Types

- **Set removal:**  
  Represented by a line starting with `-`, followed by the **three cards** in the set (in the order they appeared on the table).

- **Adding cards (no set found):**  
  Represented by a line containing only a `+`.

### Table State

Each table state is represented by a line starting with `=`, followed by all cards on the table in order.

---

### Example Output Format (Grammar)

```
Result → Table MoveSequence
MoveSequence → ε | Move Table MoveSequence
Table → '=' CardSequence '\n'
CardSequence → ε | ' ' Card CardSequence
Card → Digit Digit Digit Digit
Digit → '1' | '2' | '3'
Move → Set | Add
Set → '-' CardSequence '\n'
Add → '+' '\n'
```

**Terminals:**  
`=`, `-`, `+`, `' '`, `'1'`, `'2'`, `'3'` are literal characters.  
`\n` indicates a new line.
