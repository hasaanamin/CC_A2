# CS4031: LL(1) Parser Report
## Group: 23i-0665 Muhammad Tabarak Cheema , 23i-0019 Hasaan Amin

### 1. Introduction
This project implements an LL(1) predictive parsing engine equipped to handle automated grammar transformations. Top-down predictive parsers require grammar strictly devoid of left-recursion and common prefixes. Our tool achieves this by performing recursive left-factoring, substituting indirect left-recursion with direct forms, and finally purging the direct recursive routes, transforming raw grammars into strict LL(1) tables. 

### 2. Approach
* **Data Structures:** The `Grammar` class holds mappings between non-terminals (Strings) and production rules (`List<List<String>>`). `firstSets` and `followSets` utilize `LinkedHashSet`s to guarantee iteration ordering while preserving distinct terminal bounds. The `ParserStack` internally wraps `ArrayList<String>` to allow arbitrary indexed introspection without disrupting standard O(1) top/pop operations.
* **Algorithmic Flow:** Part 1 applies Left-Factoring until `changed=false`, feeds it to Left Recursion (Indirect -> Direct loop), calculates `FIRST` fixed points, evaluates `FOLLOW` using `FIRST`, and builds the Table `M[NT][T]`. Part 2 operates the 4-case loop (Accept, Match, Expand, Error) against input tokens using the LL(1) table coordinates.
* **Indirect Left Recursion:** Iterates across ordered Non-Terminals. For every `i` and `j < i`, it replaces `Ai -> Aj γ` with all expansions of `Aj γ`, effectively elevating deeper recursive linkages to head-level direct recursion, which is then handled by standard direct recursion stripping algorithms appending `Prime` postfix states.
* **Error Recovery Strategy:** Panic Mode Recovery utilizing `FOLLOW` sets was implemented. If an empty table entry is hit, the parser pulls the `FOLLOW` set for the mismatched non-terminal off the stack. If the currently observed input token exists within that `FOLLOW` sync-set, the parser assumes the non-terminal vanished (epsilon) and pops it to sync. Otherwise, it throws away the mismatched token and consumes the next input string character until syncing is achieved.

### 3. Challenges
The biggest challenge occurred during Indirect Left Recursion injection. When replacing `Ai -> Aj \alpha`, if `Aj` evaluated identically down to `\epsilon`, `Ai` had to seamlessly absorb empty strings or correctly graft `\alpha` prefixes without introducing double-epsilon bugs. The ASCII abstract-syntax-tree builder occasionally produced visually malformed forks when handling large `ExprPrime` tails containing sequential unary closures. We fixed this by running a secondary DFS pass to mark valid horizontal trailing caps. 

### 4. Test Cases
1. `grammar1.txt` (Expression Grammar) - Contains cascading multiplicative operators. `input_valid.txt` evaluates standard equations. `input_errors.txt` triggers panic mode via missing binary operands (`id + * id`).
2. `grammar2.txt` (Dangling Else) - `Stmt -> if Expr then Stmt | if Expr then Stmt else Stmt`. Resolves the LL conflict by pushing the `else` lookahead aggressively into the factoring pipeline.
3. `grammar7.txt` - Mandates both Left Recursion and Factoring passes continuously overlapping. `List -> List , Element | Element`. Resolves correctly. 
4. Valid parses verified via PostOrder output matching RPN forms exactly.

### 5. Verification 
Verification utilized the `isLL1()` boolean trapdoor. If any cell `M[A][a]` contained `size() > 1`, parsing would flag unstable. Output verification relied on post-order traversal validations of the generated `Tree` components. If a tree traversal didn't exactly output the consumed input, the parsing step was considered invalid. 

### 6. Conclusion
Implementing a textbook parser reveals the intense string-theory boundaries underlying modern compilers. The cascading effects of left factoring deeply altering non-terminal signatures made building robust `FIRST` sets tricky, primarily mapping heavily mutated `Prime` non-terminals back to their semantic terminal limits. However, panic mode error recovery via FOLLOW synchronizers provides extremely fluid and reliable fallback handling, allowing continuous analysis of heavily malformed codebase tokens.
