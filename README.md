# CS4031 - Compiler Construction: Assignment 02
## LL(1) Parser Design & Implementation 

### Team Members
- 23i-0665 Muhammad Tabarak Cheema (Section C)
- 23i-0019 Hasaan Amin (Section C)

### Programming Language
- **Java**

### Compilation Instructions
This project has no external dependencies. To compile from the `src` directory, you can use the standard `javac` command:
```bash
cd 23i0665-23i0019-C/src
javac *.java
```

### Execution Instructions
Run the `Main` class. You can optionally specify a grammar file and an input strings file.
```bash
# 1. To see grammar transformations, FIRST/FOLLOW, and Table:
java Main ../input/grammar1.txt

# 2. To also parse an input file using the stack-based parser:
java Main ../input/grammar1.txt ../input/input_valid.txt

# 3. To specify an output file for the results:
java Main ../input/grammar1.txt ../input/input_valid.txt ../output/results.txt

# 4. To run only Part 1 (Grammar) and save output without parsing input:
java Main ../input/grammar1.txt NONE ../output/grammarOut.txt
```

### Input File Formats
1. **Grammar Format (`.txt`)**
   - Each production on a new line: `NonTerminal -> prod1 | prod2`
   - Epsilon is represented as `@` or `epsilon`.
   - Start symbol is the NonTerminal on the first line.
   - Non-terminals start with an uppercase letter (e.g., `Expr`, `Term`). Single-letter variables are forbidden by requirements.

2. **Input Strings Format (`.txt`)**
   - Each input sequence should be on its own line.
   - Tokens within the sequence must be **separated by spaces** (e.g. `id + id * id`).

### Sample Input Files Explained
`grammar1.txt` provides an ambiguous arithmetic expression grammar. 
`input_valid.txt` tests strings that are syntactically correct according to grammar1.
`input_errors.txt` includes missing variables and unexpected operator tokens to verify panic mode syncing on `FOLLOW` sets.
`input_edge_cases.txt` includes massive expression chains to stress test loop thresholds and nesting trees.

### Known Limitations
- Left Factoring and Recursion loops run until fixed-point stabilization. If a grammar introduces circular unit productions without epsilon (e.g., A -> A), loops are caught and warned, skipping recursive injection to avoid OutOfMemory stack blows.
- Indented parse tree format assumes terminal names fit within console width.
