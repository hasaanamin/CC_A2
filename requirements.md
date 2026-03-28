# CS4031 – Compiler Construction | Assignment 02
## LL(1) Parser Design & Implementation – Spring 2026

---

> **Deadline:** As per GCR  
> **Submission:** `RollNumber1-RollNumber2-Section.zip` (e.g., `22i1234-22i5678-A.zip`)  
> **Language:** C, C++, or Java (choose one, stay consistent)

---

## General Instructions

1. Work in the **same group** assigned in Assignment 1 (groups cannot be changed).
2. Must be implemented in **C, C++, or Java** (those who chose Java/C++ for Assignment 1 can keep the same language).
3. Use of **lex or yacc is strictly prohibited**.
4. Proper **file handling** and properly formatted outputs.
5. Proper **informative comments** and consistent output required.
6. **Modularize** code into functions.
7. Upload to GCR as a **single zip file**.
8. Group details should be mentioned in the **top comments** of the main source file.

---

## Assignment Overview

Implement an **LL(1) Parser** that supports:
- Reading a CFG from a file
- Transformation of grammar (Left Factoring & Left Recursion Removal)
- Computing FIRST and FOLLOW sets
- Constructing an LL(1) parsing table
- Parsing input strings using a stack-based approach
- Handling errors gracefully during parsing
- Generating parse trees for accepted strings

---

---

# PART 1: Grammar Transformation & Table Construction

> This part focuses on **reading, transforming, and analyzing** the context-free grammar
> and producing the LL(1) parsing table.

---

### Task 1.1 – Input CFG

- Read a CFG from a **text file** (one production per line).
- Format: `NonTerminal -> production1 | production2 | ...`
- **Terminals:** lowercase letters, operators, keywords.
- **Non-terminals:** Multi-character names starting with uppercase.
- **Single-character non-terminals** (E, T, F, etc.) are **NOT allowed**.
- **Epsilon:** use `epsilon` or `@`.

---

### Task 1.2 – Left Factoring

- **Purpose:** Eliminate common prefixes in productions.
- **Output:** Display the grammar **after** left factoring is applied.

---

### Task 1.3 – Left Recursion Removal

- Must handle **both Direct and Indirect** left recursion.
- **Output:** Display the grammar **after** left recursion removal.

---

### Task 1.4 – FIRST Set Computation

- Compute FIRST sets for **all non-terminals**.
- **Output:** Display FIRST sets in **tabular form**.

---

### Task 1.5 – FOLLOW Set Computation

- Compute FOLLOW sets for **all non-terminals**.
- **Output:** Display FOLLOW sets in **tabular form**.

---

### Task 1.6 – LL(1) Parsing Table Construction

- Construct table `M[A, a]` using FIRST and FOLLOW sets.
- Indicate if grammar **is LL(1) or not** (handle multiple entries / conflicts).
- **Output:** Display the **complete parsing table**.

---

### Part 1 – Evaluation Breakdown (40 marks)

| Component                         | Marks |
|-----------------------------------|-------|
| Left Factoring                    | 5     |
| Direct Left Recursion Removal     | 5     |
| Indirect Left Recursion Removal   | 5     |
| FIRST & FOLLOW Set Computation    | 15    |
| Parsing Table Construction        | 10    |

---

---

# PART 2: Stack-Based Parser, Error Handling & Parse Tree

> This part focuses on the **runtime parsing engine**, error recovery,
> and producing visual output of the derivation.

---

### Task 2.1 – Input String Processing

- Read input strings from a file (e.g., `input.txt`).
- Tokens separated by **spaces**.

---

### Task 2.2 – Stack Implementation

- Initialize stack with `$` (bottom marker) and the **start symbol**.
- Implement **push**, **pop**, **top**, and **isEmpty** operations.

---

### Task 2.3 – Parsing Algorithm Implementation

- Implement the core LL(1) parsing loop with case-based expansion and matching logic.
- **Output:** Step-by-step **trace** showing:
  - **Stack** contents
  - **Input** remaining
  - **Action / Production** applied

---

### Task 2.4 – Error Handling & Recovery

- Detect **missing/unexpected symbols** or **empty table entries**.
- **Strategy:** Panic-Mode Recovery using synchronizing sets (FOLLOW).
- **Output:** Report syntax errors with **line/column numbers** and **skipped tokens**.

---

### Task 2.5 – Parse Tree Generation

- Represent the derivation for **successfully parsed strings**.
- **Output:** Readable tree format — **ASCII art**, **indented text**, or **DOT format**.

---

### Part 2 – Evaluation Breakdown (50 marks)

| Component                          | Marks |
|------------------------------------|-------|
| Stack Implementation               | 5     |
| Parsing Algorithm Logic            | 15    |
| Step-by-Step Trace                  | 5     |
| Error Handling & Recovery           | 20    |
| Parse Tree Generation              | 5     |

---

---

# Deliverables

| Folder     | Contents                                                        |
|------------|-----------------------------------------------------------------|
| `src/`     | Separated files: Grammar, Sets, Parser, Stack, Tree, ErrorHandler |
| `input/`   | Multiple grammars + valid/invalid input files                   |
| `output/`  | Transformed grammar, sets, table, traces, and trees             |
| `docs/`    | `report.pdf` – Intro, Approach, Challenges, Test Cases, Verification, Conclusion |
| Root       | `README.md` – Compilation instructions and project details      |

---

# Additional Evaluation

| Component                    | Marks |
|------------------------------|-------|
| Code Structure & Comments    | 5     |
| Report                       | 10    |
| **Total**                    | **100** |

---

# Important Requirements

- **No plagiarism**; groups of exactly **2**.
- **No AI-generated code.**
- Language must be **consistent** (C, C++, or Java throughout).

---

# Common Pitfalls to Avoid

- Incorrect **epsilon handling** in sets or transformations.
- **Infinite loops** in transformation or error recovery.
- **Memory leaks** (C/C++) or **stream management** (Java).
- Parse trees **not reflecting actual derivations**.
