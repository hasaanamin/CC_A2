/*
 * Parser.java
 * CS4031 - Compiler Construction, Assignment 02
 * LL(1) Parser Design & Implementation - Spring 2026
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Stack-based LL(1) predictive parser implementation.
 *   - Reads input strings from a file (tokens separated by spaces)
 *   - Uses the LL(1) parsing table to drive the parse
 *   - Generates step-by-step parsing traces
 *   - Builds parse trees for accepted strings
 *   - Integrates with ErrorHandler for panic-mode recovery
 */

import java.io.*;
import java.util.*;

public class Parser {

    private ParsingTable parsingTable;
    private Grammar grammar;
    private Map<String, Set<String>> firstSets;
    private Map<String, Set<String>> followSets;

    /**
     * Constructs a Parser with the required components.
     *
     * @param grammar      the transformed grammar
     * @param parsingTable the LL(1) parsing table
     * @param firstSets    FIRST sets
     * @param followSets   FOLLOW sets
     */
    public Parser(Grammar grammar, ParsingTable parsingTable,
                  Map<String, Set<String>> firstSets,
                  Map<String, Set<String>> followSets) {
        this.grammar = grammar;
        this.parsingTable = parsingTable;
        this.firstSets = firstSets;
        this.followSets = followSets;
    }

    /**
     * Reads input strings from a file and parses each one.
     * Each line in the file is treated as a separate input string.
     *
     * @param inputFilePath  path to the input file
     * @param outputFilePath path to write results
     */
    public void parseFile(String inputFilePath, String outputFilePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                continue;
            }

            System.out.println("\n" + "=".repeat(70));
            System.out.println("  Parsing Input Line " + lineNumber + ": " + line);
            System.out.println("=".repeat(70));

            ParseResult result = parseString(line, lineNumber);

            // Display result
            result.displayTrace();
            if (result.accepted && result.parseTree != null) {
                result.parseTree.display("Parse Tree for: " + line);
            }
            result.displayResult();

            // Write to file
            result.writeToFile(outputFilePath, line);
        }
        reader.close();
    }

    /**
     * Parses a single input string using the LL(1) parsing algorithm.
     *
     * @param input      the input string (tokens separated by spaces)
     * @param lineNumber the line number in the input file
     * @return ParseResult containing trace, tree, and error info
     */
    public ParseResult parseString(String input, int lineNumber) {
        // Tokenize the input
        String[] tokens = input.trim().split("\\s+");
        List<String> inputTokens = new ArrayList<>(Arrays.asList(tokens));
        // Append $ as end marker if not already there
        if (inputTokens.isEmpty() || !inputTokens.get(inputTokens.size() - 1).equals("$")) {
            inputTokens.add("$");
        }

        // Initialize the parser stack
        ParserStack stack = new ParserStack(grammar.getStartSymbol());

        // Initialize error handler
        ErrorHandler errorHandler = new ErrorHandler(followSets, grammar.getTerminals());

        // Trace steps
        List<TraceStep> trace = new ArrayList<>();

        // Parse tree nodes tracking
        // We use a parallel stack of TreeNode references to build the tree
        Tree.TreeNode rootNode = new Tree.TreeNode(grammar.getStartSymbol(), false);
        Deque<Tree.TreeNode> nodeStack = new ArrayDeque<>();
        // nodeStack mirrors the parser stack (excluding $)
        nodeStack.push(rootNode);

        int inputPointer = 0;
        int stepNumber = 0;
        boolean accepted = false;
        boolean errorOccurred = false;

        // Safety limit to prevent infinite loops in error recovery
        int maxSteps = inputTokens.size() * 100 + 500;

        while (stepNumber < maxSteps) {
            stepNumber++;

            String stackTop = stack.top();
            String currentInput = (inputPointer < inputTokens.size())
                    ? inputTokens.get(inputPointer) : "$";

            String stackContents = stack.getContents();
            String remainingInput = buildRemainingInput(inputTokens, inputPointer);

            // Case 1: Both stack top and input are $  =>  ACCEPT
            if (stackTop.equals("$") && currentInput.equals("$")) {
                trace.add(new TraceStep(stepNumber, stackContents,
                        remainingInput, "ACCEPT"));
                accepted = true;
                break;
            }

            // Case 2: Stack top matches current input (both are same terminal)  =>  MATCH
            if (stackTop.equals(currentInput) && !stackTop.equals("$")) {
                trace.add(new TraceStep(stepNumber, stackContents,
                        remainingInput, "Match '" + stackTop + "'"));
                stack.pop();

                // Mark the corresponding tree node as matched terminal
                if (!nodeStack.isEmpty()) {
                    nodeStack.pop(); // consumed
                }

                inputPointer++;
                continue;
            }

            // Case 3: Stack top is a non-terminal  =>  look up table
            if (grammar.isNonTerminal(stackTop)) {
                Map<String, List<String>> row = parsingTable.getTable().get(stackTop);
                List<String> entries = null;
                if (row != null) {
                    entries = row.get(currentInput);
                }

                if (entries != null && !entries.isEmpty()) {
                    // Use the first entry (should be only one for LL(1))
                    String production = entries.get(0);
                    trace.add(new TraceStep(stepNumber, stackContents,
                            remainingInput, production));

                    // Parse the production: "A -> sym1 sym2 ..."
                    String[] prodParts = production.split("->");
                    String rhsStr = prodParts[1].trim();
                    String[] rhsSymbols = rhsStr.split("\\s+");

                    // Pop the non-terminal from stack
                    stack.pop();
                    Tree.TreeNode parentNode = nodeStack.isEmpty() ? null : nodeStack.pop();

                    // If RHS is epsilon
                    if (rhsSymbols.length == 1 && rhsSymbols[0].equals("@")) {
                        // Add epsilon child to tree
                        if (parentNode != null) {
                            Tree.TreeNode epsilonNode = new Tree.TreeNode("@", true);
                            parentNode.addChild(epsilonNode);
                        }
                    } else {
                        // Push symbols in reverse order onto stack
                        // Build tree children in forward order
                        List<Tree.TreeNode> childNodes = new ArrayList<>();
                        for (String sym : rhsSymbols) {
                            boolean isTerm = !grammar.isNonTerminal(sym);
                            Tree.TreeNode childNode = new Tree.TreeNode(sym, isTerm);
                            childNodes.add(childNode);
                            if (parentNode != null) {
                                parentNode.addChild(childNode);
                            }
                        }

                        // Push in reverse onto both stacks
                        for (int i = rhsSymbols.length - 1; i >= 0; i--) {
                            stack.push(rhsSymbols[i]);
                            nodeStack.push(childNodes.get(i));
                        }
                    }
                    continue;
                } else {
                    // ERROR: Empty table entry M[stackTop, currentInput]
                    errorOccurred = true;
                    int col = inputPointer + 1;

                    String recovery = errorHandler.panicModeRecover(stackTop, currentInput);

                    if (recovery.equals("POP")) {
                        // Synchronize by popping the non-terminal
                        String action = "ERROR: No entry M[" + stackTop + ", " + currentInput
                                + "]. Pop '" + stackTop + "' (sync on FOLLOW)";
                        trace.add(new TraceStep(stepNumber, stackContents,
                                remainingInput, action));
                        errorHandler.unexpectedSymbol(lineNumber, col, stackTop,
                                currentInput, "Popped '" + stackTop + "' from stack");

                        stack.pop();
                        if (!nodeStack.isEmpty()) {
                            nodeStack.pop();
                        }
                    } else {
                        // Skip the input symbol
                        String action = "ERROR: No entry M[" + stackTop + ", " + currentInput
                                + "]. Skipping '" + currentInput + "'";
                        trace.add(new TraceStep(stepNumber, stackContents,
                                remainingInput, action));
                        errorHandler.unexpectedSymbol(lineNumber, col, stackTop,
                                currentInput, "Skipped '" + currentInput + "' in input");

                        inputPointer++;
                        if (inputPointer >= inputTokens.size()) {
                            // Input exhausted during error recovery
                            trace.add(new TraceStep(stepNumber + 1, stack.getContents(),
                                    "$", "ERROR: Input exhausted during recovery"));
                            break;
                        }
                    }
                    continue;
                }
            }

            // Case 4: Stack top is a terminal that doesn't match input  =>  ERROR
            if (!stackTop.equals("$")) {
                errorOccurred = true;
                int col = inputPointer + 1;

                String action = "ERROR: Expected '" + stackTop + "', found '" + currentInput
                        + "'. Popping '" + stackTop + "'";
                trace.add(new TraceStep(stepNumber, stackContents,
                        remainingInput, action));

                errorHandler.missingSymbol(lineNumber, col, stackTop, currentInput);

                // Recovery: pop the expected terminal (insertion recovery)
                stack.pop();
                if (!nodeStack.isEmpty()) {
                    nodeStack.pop();
                }
                continue;
            }

            // Shouldn't reach here normally
            break;
        }

        // If we exhausted steps, note the issue
        if (stepNumber >= maxSteps) {
            System.err.println("  WARNING: Parsing exceeded maximum step limit.");
        }

        // Build result
        Tree parseTree = accepted ? new Tree(rootNode) : null;
        // Even if not fully accepted, provide tree if partially built
        if (!accepted && errorOccurred) {
            parseTree = new Tree(rootNode); // partial tree for error cases
        }

        return new ParseResult(trace, accepted, errorOccurred, errorHandler, parseTree);
    }

    /**
     * Builds a string of the remaining input from the pointer position.
     */
    private String buildRemainingInput(List<String> tokens, int pointer) {
        StringBuilder sb = new StringBuilder();
        for (int i = pointer; i < tokens.size(); i++) {
            if (i > pointer) sb.append(" ");
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    // ===== Inner class: TraceStep =====

    /**
     * Represents one step of the parsing trace.
     */
    public static class TraceStep {
        int stepNumber;
        String stackContents;
        String remainingInput;
        String action;

        public TraceStep(int step, String stack, String input, String action) {
            this.stepNumber = step;
            this.stackContents = stack;
            this.remainingInput = input;
            this.action = action;
        }

        @Override
        public String toString() {
            return String.format("  %-5d | %-35s | %-25s | %s",
                    stepNumber, stackContents, remainingInput, action);
        }
    }

    // ===== Inner class: ParseResult =====

    /**
     * Encapsulates the result of parsing a single input string.
     */
    public static class ParseResult {
        List<TraceStep> trace;
        boolean accepted;
        boolean errorOccurred;
        ErrorHandler errorHandler;
        Tree parseTree;

        public ParseResult(List<TraceStep> trace, boolean accepted,
                           boolean errorOccurred, ErrorHandler errorHandler,
                           Tree parseTree) {
            this.trace = trace;
            this.accepted = accepted;
            this.errorOccurred = errorOccurred;
            this.errorHandler = errorHandler;
            this.parseTree = parseTree;
        }

        /**
         * Displays the parsing trace to the console.
         */
        public void displayTrace() {
            System.out.println("\n  Parsing Trace:");
            System.out.printf("  %-5s | %-35s | %-25s | %s%n",
                    "Step", "Stack", "Input", "Action");
            System.out.println("  " + "-".repeat(100));

            for (TraceStep step : trace) {
                System.out.println(step.toString());
            }
        }

        /**
         * Displays the final result (accept/reject).
         */
        public void displayResult() {
            System.out.println();
            if (accepted && !errorOccurred) {
                System.out.println("  >> Result: String ACCEPTED successfully!");
            } else if (accepted && errorOccurred) {
                System.out.println("  >> Result: Parsing completed with "
                        + errorHandler.getErrorCount() + " error(s).");
                errorHandler.displayErrors();
            } else {
                System.out.println("  >> Result: String REJECTED.");
                errorHandler.displayErrors();
            }
        }

        /**
         * Writes the trace and result to an output file.
         */
        public void writeToFile(String filePath, String input) throws IOException {
            PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));

            writer.println("\n" + "=".repeat(70));
            writer.println("  Input: " + input);
            writer.println("=".repeat(70));

            // Trace table
            writer.println("\n  Parsing Trace:");
            writer.printf("  %-5s | %-35s | %-25s | %s%n",
                    "Step", "Stack", "Input", "Action");
            writer.println("  " + "-".repeat(100));
            for (TraceStep step : trace) {
                writer.println(step.toString());
            }

            // Result
            writer.println();
            if (accepted && !errorOccurred) {
                writer.println("  >> Result: String ACCEPTED successfully!");
            } else if (accepted && errorOccurred) {
                writer.println("  >> Result: Parsing completed with "
                        + errorHandler.getErrorCount() + " error(s).");
            } else {
                writer.println("  >> Result: String REJECTED.");
            }

            // Errors
            if (errorHandler.hasErrors()) {
                writer.println();
                for (ErrorHandler.SyntaxError err : errorHandler.getErrors()) {
                    writer.println("  " + err.message);
                }
            }

            // Parse tree (written inline to keep output order correct)
            if (parseTree != null) {
                writer.println("\n" + "=".repeat(60));
                writer.println("  Parse Tree for: " + input);
                writer.println("=".repeat(60));
                writer.println(parseTree.toASCIIArt());
                writer.println("  Indented Text Format:");
                writer.println(parseTree.toIndentedText());
                writer.println("  Preorder:  " + String.join(" ", parseTree.preorderTraversal()));
                writer.println("  Postorder: " + String.join(" ", parseTree.postorderTraversal()));
                writer.println("=".repeat(60));
            }

            writer.close();
        }
    }
}
