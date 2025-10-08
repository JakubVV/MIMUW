# Lotto System (Totolotek)
**Object-Oriented Programming 2024L — Version 1.00**

---

## Specification Overview

### General Description

The goal of this assignment is to design and implement a set of **classes** modeling a **Lotto system (Totolotek)**.

Totolotek sells **bets** that allow players to participate in periodic drawings of **6 unique numbers from 1 to 49**.  
A **bet** represents one selection of 6 numbers made by the player.

Players purchase bets only through a network of **retail outlets** (called *agencies* or *points of sale*) and only in the form of **anonymous tickets**.  
A single ticket can contain multiple bets and can cover more than one draw.

- A **single bet costs 3 PLN**, of which **0.60 PLN is tax** (paid to the state).
- The remaining **2.40 PLN** goes to Totolotek’s central office (for prizes and profits).

Prizes are awarded for 3, 4, 5, or 6 correct numbers in a bet — referred to as **IV**, **III**, **II**, and **I** prize tiers respectively.

The sections below describe all system components in detail.

---

## Central Office

The **Central Office** manages all draws and finances.  
It can receive **state subsidies** and must:

- Conduct official draws of 6 distinct numbers.
- After each draw, **calculate the total prize pool** and the **prize amounts** for each tier (I–IV).

Calculations are based on:
- Total sales revenue (minus tax) across all agencies,
- Number of bets matching each prize tier,
- Possible **jackpots (rollovers)**.

### Jackpot Mechanism
If no bet wins the first prize (6 correct numbers), the **entire first prize pool rolls over** to the next draw’s first prize pool.

### Public Information
The following must be publicly accessible:
- Winning numbers,
- Total pool for the first prize tier,
- Prize amounts for each tier that had winners.

The Central Office must also be able to print:
- Results of all past draws,
- Number of winners per tier,
- Total pool per tier,
- Current financial balance.

---

## Prize Collection

Players collect winnings at the **agency** where their ticket was purchased.

The system must verify:
1. The ticket was legitimately issued by that agency.
2. The prize for that ticket has not already been paid.

Once redeemed, a ticket is **invalidated** (marked as used).  
If a ticket is redeemed before all its draws are completed, **future draws are forfeited**.

### Taxation
- **High prizes** (≥ 2,280 PLN per winning bet per draw) are taxed **10%**, deducted at payout.
- **Lower prizes** are **tax-free**.
- Only **high prizes** are taxed — even if a ticket has multiple wins.

If the Central Office runs out of money to pay prizes, it must **request a subsidy** from the state budget for the missing amount.

---

## Draw

A **Draw** represents a single official lottery drawing.

- Each draw has a **unique sequential number** (starting from 1).
- Stores the **6 drawn numbers** in **ascending order**.
- The printed format must include:
```

Draw No. 6901
Results:  5  8  9 28 31 47

```

---

## Prize Distribution

51% of the **total revenue (minus tax)** from all bets in a draw goes toward prizes.  
The remaining 49% is retained as Totolotek’s profit.

Prize distribution:
- **44%** → First-tier (6 hits),
- **8%** → Second-tier (5 hits),
- **Each fourth-tier prize (3 hits)** = **24.00 PLN** (fixed),
- The **remaining** amount → Third-tier (4 hits).

Additional rules:
- Minimum first-tier pool = **2,000,000 PLN** (guaranteed).
- Each pool (I–III) is divided equally among all winning bets in that tier.
- Each third-tier prize must be at least **15 × 2.40 PLN = 36.00 PLN**.
- Rollover applies if there are no first-tier winners.
- If the central office cannot pay prizes, it receives subsidies from the state.

All monetary calculations **must round down** to the nearest grosz (cent).

---

## Agency (Retail Point)

An **Agency** sells tickets and handles payouts.

- Each agency has a **unique ID**.
- Stores all sold tickets.
- Sends all **profits** immediately to the central office.
- Requests **prize funds** from the central office for payouts.

### Ticket Types
1. **Manual tickets** — based on a filled-out **play slip (blank form)**.
2. **Quick picks (random tickets)** — randomly generated sets of numbers.

Each transaction sells **one ticket** only, and only if the player has enough funds.

### Financial Flow
- From each sale:
- **Tax (0.60 PLN per bet)** → state budget,
- **Net (2.40 PLN per bet)** → central office.

Tickets are recorded in the agency database and only issued **after successful payment**.

---

## Play Slip (Blank Form)

A **play slip** is a paper form used to generate a ticket.

- Contains **8 numbered fields** (1–8), each for one bet.
- Each field has **49 boxes** (1–49) and one box marked **“cancel”**.
- A valid bet has **exactly 6 checked boxes**.  
Invalid fields (≠ 6 numbers or canceled) are ignored.

At the bottom are **10 boxes labeled “Number of Draws” (1–10)**:
- One box can be marked — if none, defaults to 1 draw.
- If multiple are marked, use the **highest** number.

---

## Ticket

A **Ticket** is the only proof of participation and is required to collect prizes.

### Identification
Each ticket has a globally unique ID:
```

<ticket_no>-<agency_no>-<random_marker>-<checksum>

```
Example: `1959-790-959497998-09`

Where:
- `ticket_no` — unique sequential number,
- `agency_no` — agency ID,
- `random_marker` — 9 random digits,
- `checksum` — sum of digits of all components mod 100 (two digits, padded).

### Structure
- 1–8 bets (each: 6 numbers),
- Up to **10 consecutive draws**,
- The first draw is always the **next scheduled draw**,
- Price = `(number of bets × number of draws × 3 PLN)`.

### Ticket Information (Printable)
Includes:
1. Ticket ID  
2. Numbered list of bets (numbers aligned right)  
3. Number of draws  
4. List of draw numbers  
5. Total ticket price  

Example:
```

TICKET NO. 5-125-611233269-46
1: 11 12 19 23 33 43
2:  4 15 24 33 35 44
NUMBER OF DRAWS: 4
DRAW NUMBERS: 8 9 10 11
PRICE: 24 PLN 00 gr

```

---

## Player

A **Player** can:
- Buy tickets at agencies,
- Hold funds and a list of owned tickets,
- Redeem winnings.

Each player has:
- First name, last name, PESEL (no validation required),
- Current balance,
- List of owned tickets.

Player information includes:
- Name and PESEL,
- Available funds,
- List of ticket IDs (or “no tickets”).

---

### Player Types

1. **Minimalist Player**
   - Always buys one *quick-pick* ticket,
   - For one draw,
   - Always at their favorite agency.

2. **Random Player**
   - Chooses a random agency,
   - Buys 1–100 *quick-pick* tickets,
   - Each with a random number of bets and draws,
   - Starts with a random balance (< 1,000,000 PLN).

3. **Fixed-Number Player**
   - Has 6 favorite numbers,
   - Fills a new play slip for 10 consecutive draws,
   - Buys a new ticket only after previous draws finish.

4. **Fixed-Slip Player**
   - Has a pre-filled play slip,
   - Buys tickets based on it every fixed number of draws,
   - Rotates through several favorite agencies.

The system must allow **easy addition of new player types**.

---

## State Budget

- Collects taxes and issues subsidies.
- Tracks:
  - Total collected taxes,
  - Total subsidies paid.
- The budget is effectively unlimited (no cap modeled).

---

## Task Requirements

Implement classes to fulfill the above specification, and provide:
- **Main program method** demonstrating class behavior,
- **JUnit tests** verifying logic.

Submit:
- Source files (`.java`) organized into packages,
- Compressed as `src.zip`.

Do **not** include other files.

---

## Demonstration (Main Method)

Your program should:

1. Create:
   - Central office,
   - 10 agencies.
2. Create:
   - 200 players of each type (≈ evenly distributed).
3. Conduct:
   - 20 consecutive draws.
4. Before each draw:
   - Players purchase tickets.
5. After each draw:
   - Each player checks if any ticket has finished all its draws,
   - If so, and if it has a prize → collect winnings.

### Final Output Must Include:
- Complete report from the central office (all draws, prizes, pools, etc.),
- Total taxes collected by the state,
- Total subsidies granted to the central office.

---

## JUnit Tests

Implement JUnit tests covering:
- Logic unrelated to randomness:
  - Play slip parsing and validation,
  - Ticket creation and error detection,
  - Prize calculations for all tiers (including rollovers),
  - Ticket sales (balance changes, tax payments).

Focus on deterministic and financial correctness, not random draws.

---
