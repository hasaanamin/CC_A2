# CS4031: LL(1) Parser Implementation Report
## Group Details
**Student 1:** Muhammad Tabarak Cheema (23i-0665)  
**Student 2:** Hasaan Amin (23i-0019)  
**Section:** C  
**Date:** Spring 2026  

---

## 1. Introduction
This project focuses on the development of an LL(1) predictive parsing engine. Predictive top-down parsing approaches, like LL(1), require context-free grammars (CFGs) to adhere to strict geometric limitations—specifically, they must be unambiguous, fully devoid of left-recursion (both direct and indirect), and thoroughly left-factored to eliminate overlapping prefix derivatives. 

Our implementation automatically transforms generic user-provided raw grammars into strict LL(1) compliant tables. The project achieves this by running recursive Left-Factoring algorithms, detecting and unwinding Indirect Left Recursion into Direct Left Recursion, and subsequently stripping Direct Left Recursion via immediate substitution. 

## 2. Approach and Methodology

### Data Structures
- **The Grammar Wrapper:** Handled by a dedicated object storing mappings from Non-Terminals (Strings) onto a sequence of available permutations (`List<List<String>>`). 
- **Set Implementations:** `LinkedHashSet<String>` was strictly utilized across the system to preserve chronological definition logic ensuring terminals weren’t chaotically scrambled during table generation.
- **The Parser Stack:** Instead of a bare array, we utilized `ArrayList<String>` enveloped in a custom `ParserStack` API. The index-zero acts as the bottom marker (`$`).

### Algorithm Implementation Details
1. **Left-Factoring Iteration:** Groups alternatives sharing identical terminal/non-terminal starting nodes. Factoring establishes an immediate unified prefix and offloads divergent suffixes onto a dynamically fabricated `Prime` state until a fixed-point convergence confirms no overlapping prefixes remain.
2. **First/Follow Computation:** Employs a fixed-point evaluation cycle. `FIRST` aggregates token derivations independently, feeding straight into `FOLLOW` propagation mapping trailing identifiers.

### Design Decisions
A critical design decision was separating AST generation logic alongside the parsing trace algorithm dynamically tracking parallel stacks. Instead of building the tree after parsing completes, a secondary stack `Deque<Tree.TreeNode> nodeStack` mirrors the main parser state.

### Indirect Left-Recursion Handling Approach
Driven by indexing Non-Terminals explicitly into an iterable ordered vector $A_1, A_2 ... A_n$. For any pair pairs $(i, j)$ where $j < i$, productions conforming to $A_i \rightarrow A_j \gamma$ absorb all $A_j$ derivatives dynamically, elevating obscure cyclic relationships into visible Direct Recursion pipelines. Direct recursion is immediately destroyed by generating an $A_{Prime}$, redirecting base routes.

### Error Recovery Strategy
We utilized **Panic-Mode Synchronization**:
1. If the scanner observes a character belonging to the current non-terminal's `FOLLOW` bounding set, it is assumed the targeted block is absent/empty, popping the non-terminal to aggressively re-sync the structural geometry.
2. If the scanned character violates even the `FOLLOW` expectations, the token is permanently consumed and discarded (`SKIP`). 

## 3. Challenges
- **Infinite Epsilon Loops During Injection:** Unwinding indirect left recursion periodically encountered recursive states deriving identically down to $\epsilon$. This created double-$\epsilon$ concatenations which falsely terminated branches. We implemented localized $\epsilon$-flattening logic preventing $\epsilon \cdot \gamma$ expansions.
- **Parse-Tree ASCII Misalignment:** Rendering massive geometric derivations using strict recursive ASCII tracking created "floating" node tails. A parallel node monitoring stack inside the core trace iterator fixed the spatial coordinates automatically.

## 4. Test Cases
The system was executed against 3 unique environments, testing exactly 5 strings each:

**Grammar 1 (Arithmetic Expressions - `grammar1.txt`):**
1. Valid: `id + id * id`
2. Valid: `( id + id ) * id`
3. Valid: `id * id + id`
4. Error: `id + * id` (Tests `MISSING_SYMBOL`)
5. Error: `id + id *` (Tests `PREMATURE_END`)

**Grammar 2 (If-Else Flow Control - `grammar2.txt`):**
1. Valid: `if id then assign`
2. Valid: `if num then assign else assign`
3. Valid: `if id then if num then assign else assign`
4. Error: `if then assign` (Tests parsing collision logic)
5. Error: `assign else assign` (Tests trailing mismatch errors)

**Grammar 7 (Arguments / Indirect Recursion - `grammar7.txt`):**
1. Valid: `id`
2. Valid: `id , id`
3. Valid: `id ( id , num , id )`
4. Error: `id ( )` (Missing Argument payload test)
5. Error: `id , , id` (Double comma syntax check)

## 5. Verification
Verification utilized autonomous traps checking the LL(1) matrix generator density: `isLL1 = (List<String> entries.size() <= 1)`. Conflict logging confirmed successful resolution on all supported CFGs. Post-order traversal validation of the `Tree.java` objects cleanly recompiled consumed input token blocks exactly tracking sequential derivations to prove output accuracy natively.

## 6. Sample Outputs

### Screenshots of Program Execution (Terminal Output Transcripts)
```console
======================================================================
  Input: id + id * id
======================================================================

  Parsing Trace:
  Step  | Stack                               | Input                     | Action
  ----------------------------------------------------------------------------------------------------
  1     | $ Expr                              | id + id * id $            | Expr -> Term ExprPrime
  2     | $ ExprPrime Term                    | id + id * id $            | Term -> Factor TermPrime
  3     | $ ExprPrime TermPrime Factor        | id + id * id $            | Factor -> id
  4     | $ ExprPrime TermPrime id            | id + id * id $            | Match 'id'
  5     | $ ExprPrime TermPrime               | + id * id $               | TermPrime -> @
  6     | $ ExprPrime                         | + id * id $               | ExprPrime -> + Term ExprPrime
  7     | $ ExprPrime Term +                  | + id * id $               | Match '+'
  8     | $ ExprPrime Term                    | id * id $                 | Term -> Factor TermPrime
```

### Parse Trees for Accepted Strings
*Example Tree generated for valid input `id , id` (Grammar 7)*:
```text
  Parse Tree (ASCII):
  ────────────────────────────────────────
  └─ List
     ├─ Element
     │  ├─ [id]
     │  └─ ElementPrime
     │     └─ [ε]
     └─ ListPrime
        ├─ [,]
        ├─ Element
        │  ├─ [id]
        │  └─ ElementPrime
        │     └─ [ε]
        └─ ListPrime
           └─ [ε]
  ────────────────────────────────────────
```

### Error Messages and Recovery Examples
*Panic Mode Recovery output when passing `id ( )` expecting arguments inside brackets (Grammar 7):*
```text
  5     | $ ListPrime ) ArgList (             | ( ) $                     | Match '('
  6     | $ ListPrime ) ArgList               | ) $                       | ERROR: No entry M[ArgList, )]. Pop 'ArgList' (sync on FOLLOW)
  7     | $ ListPrime )                       | ) $                       | Match ')'
  8     | $ ListPrime                         | $                         | ListPrime -> @
  9     | $                                   | $                         | ACCEPT

  >> Result: Parsing completed with 1 error(s).

  ERROR at line 6, column 3: No production for M[ArgList, )]. Unexpected ')'. Action: Popped 'ArgList' from stack
```

## 7. Conclusion
Implementing a textbook parser clearly delineates the critical geometric limitations framing theoretical CFG evaluations versus dynamic programmatic language execution. The execution proved that formal validation against panic-mode synchronized buffers securely supports generalized runtime compilers.
