# CS4031: LL(1) Parser Implementation Report
## Group Details
**Student 1:** Muhammad Tabarak Cheema (23i-0665)  
**Student 2:** Hasaan Amin (23i-0019)  
**Section:** C  
**Date:** Spring 2026  

---

## 1. Introduction
This project focuses on the development of an LL(1) predictive parsing engine. Predictive top-down parsing approaches, like LL(1), require context-free grammars (CFGs) to adhere to strict geometric limitations—specifically, they must be unambiguous, fully devoid of left-recursion (both direct and indirect), and thoroughly left-factored to eliminate overlapping prefix derivatives. 

Our implementation automatically transforms generic user-provided raw grammars into strict LL(1) compliant tables. The project achieves this by running recursive Left-Factoring algorithms, detecting and unwinding Indirect Left Recursion into Direct Left Recursion, and subsequently stripping Direct Left Recursion via immediate substitution. The resultant non-terminals map directly to robust `FIRST` and `FOLLOW` analytical sets, producing an automated parsing table `M[NT][T]`. Following this, a stack-based algorithm executes the parsing derivation dynamically, tracking symbols and implementing robust "Panic-Mode" error recovery.

## 2. Approach and Methodology

### Data Structures & Memory Management
- **The Grammar Wrapper:** Handled by a dedicated object storing mappings from Non-Terminals (Strings) onto a sequence of available permutations (`List<List<String>>`). 
- **Set Implementations:** `LinkedHashSet<String>` was strictly utilized across the system to preserve chronological definition logic ensuring terminals weren’t chaotically scrambled during table generation.
- **The Parser Stack:** Instead of a bare array, we utilized `ArrayList<String>` enveloped in a custom `ParserStack` API. The index-zero acts as the bottom marker (`$`), pushing and popping strings explicitly at the tail, reducing bounds checking complexity while facilitating full O(N) introspection for Trace displays.

### Algorithmic Pipeline Implementation
1. **Left-Factoring Iteration:** Groups alternatives sharing identical terminal/non-terminal starting nodes. Factoring establishes an immediate unified prefix and offloads divergent suffixes onto a dynamically fabricated `Prime` state until a fixed-point convergence confirms no overlapping prefixes remain.
2. **Indirect Left-Recursion Substitution:** Driven by indexing Non-Terminals explicitly into an iterable ordered vector $A_1, A_2 ... A_n$. For any pair pairs $(i, j)$ where $j < i$, productions conforming to $A_i \rightarrow A_j \gamma$ absorb all $A_j$ derivatives dynamically, elevating obscure cyclic relationships into visible Direct Recursion pipelines.
3. **Direct Left-Recursion Destruction:** Segregates $\alpha$-roots (recursive branches) from $\beta$-roots (base branches). By generating an $A_{Prime}$, the engine redirects base routes toward $A_{Prime}$ iteratively consuming the $\alpha$ sequences before escaping via $\epsilon$.
4. **First/Follow Computation:** Employs a fixed-point evaluation cycle. `FIRST` aggregates token derivations independently, feeding straight into `FOLLOW` propagation mapping trailing identifiers bounding acceptable language scope.

### Error Handling Strategy: Panic-Mode Recovery
Top-down derivation expects absolute LL(1) structural matching. Any empty-cell `M[top][token]` is registered as a critical syntax divergence. To avoid crashing, we implemented *Panic-Mode Synchronization*:
1. If the scanner observes a character belonging to the current non-terminal's `FOLLOW` bounding set, it is assumed the targeted block is absent/empty, popping the non-terminal to aggressively re-sync the structural geometry.
2. If the scanned character violates even the `FOLLOW` expectations, the token is permanently consumed and discarded (`SKIP`). 
This guarantees the scanner breaks infinite cycles and successfully catches trailing cascading errors seamlessly.

## 3. Technical Challenges & Resolutions
- **Infinite Epsilon Loops During Injection:** Unwinding indirect left recursion periodically encountered recursive states deriving identically down to $\epsilon$. This created double-$\epsilon$ concatenations which falsely terminated branches. We implemented localized $\epsilon$-flattening logic preventing $\epsilon \cdot \gamma$ expansions.
- **Parse-Tree ASCII Misalignment:** Rendering massive geometric derivations using strict recursive ASCII tracking created "floating" node tails when parsing hyper-nested `ExprPrime` mathematical chains. A parallel node monitoring stack inside the core trace iterator fixed the spatial coordinates automatically.

## 4. Test Cases
The system was executed against the following environments:
1. **`grammar1.txt` (Dangling Arithmetic Expression):** Evaluates arithmetic cascades (`Term -> Term * Factor`). Valid inputs output correct RPN representations. Invalid operators (`id + * id`) dynamically panic-sync against `FOLLOW` identifiers recovering the trailing `id`.
2. **`grammar2.txt` (Conditionals - If/Else):** Evaluates traditional flow control statements, confirming left factoring handles cascading `if/then` variants cleanly tracking `else` via distinct $Prime$ tails.
3. **`grammar7.txt` (Compound Stress-Testing):** Designed explicitly to mandate intertwined Left-Factoring and Left-Recursion execution passes continuously. Outputs generated perfectly formatted derivation chains without crashing.

### Evaluation of Trace Geometry
The resulting parser traces clearly match the expected operations mapping exactly to the input sequence geometries.

## 5. Verification
System verification utilized autonomous traps: 
By checking the LL(1) matrix generator density: `isLL1 = (List<String> entries.size() <= 1)`. Conflict logging confirmed successful resolution on all supported CFGs. In parallel, Post-order traversal validation of the `Tree.java` objects cleanly recompiled consumed input token blocks exactly tracking sequential derivations.

## 6. Conclusion
Implementing a textbook parser clearly delineates the critical geometric limitations framing theoretical CFG evaluations versus dynamic programmatic language execution. The cascading systemic effects generated through grammar alteration heavily mutate the internal matrix coordinate relationships, but establishing strict LL(1) boundaries unlocks exceptionally fluid parsing capabilities matching linear computation time. The execution proved that formal validation against panic-mode synchronized buffers securely supports generalized runtime compilers.
