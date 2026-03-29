/*
 * ErrorHandler.java
 * CS4031 - Compiler Construction, Assignment 02
 * LL(1) Parser Design & Implementation - Spring 2026
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Error handling and recovery for the LL(1) parser.
 *   Implements Panic-Mode Recovery strategy:
 *     - On empty table entry: skip input tokens until a synchronizing
 *       symbol (from the FOLLOW set of the current non-terminal) is found,
 *       or pop the non-terminal from the stack.
 *     - On terminal mismatch: report and insert/skip the expected terminal.
 *   Tracks all errors with line/column information for reporting.
 */

import java.io.*;
import java.util.*;

public class ErrorHandler {

    /**
     * Represents a single syntax error detected during parsing.
     */
    public static class SyntaxError {
        int lineNumber;       // Line number in the input file (1-indexed)
        int columnNumber;     // Column/token position in the line (1-indexed)
        String errorType;     // "MISSING_SYMBOL", "UNEXPECTED_SYMBOL", "EMPTY_TABLE_ENTRY", "PREMATURE_END"
        String expected;      // What was expected
        String found;         // What was actually found
        String action;        // Recovery action taken
        String message;       // Human-readable error message

        public SyntaxError(int line, int col, String type, String expected,
                           String found, String action) {
            this.lineNumber = line;
            this.columnNumber = col;
            this.errorType = type;
            this.expected = expected;
            this.found = found;
            this.action = action;
            this.message = buildMessage();
        }

        /**
         * Builds a human-readable error message.
         */
        private String buildMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("ERROR at line ").append(lineNumber)
              .append(", column ").append(columnNumber).append(": ");

            switch (errorType) {
                case "MISSING_SYMBOL":
                    sb.append("Missing symbol. Expected '").append(expected)
                      .append("' but found '").append(found).append("'.");
                    break;
                case "UNEXPECTED_SYMBOL":
                    sb.append("Unexpected symbol '").append(found)
                      .append("'. Expected one of: ").append(expected).append(".");
                    break;
                case "EMPTY_TABLE_ENTRY":
                    sb.append("No production for M[").append(expected)
                      .append(", ").append(found).append("]. ")
                      .append("Unexpected '").append(found).append("'.");
                    break;
                case "PREMATURE_END":
                    sb.append("Premature end of input. Expected '")
                      .append(expected).append("' but input ended.");
                    break;
                default:
                    sb.append("Unknown error.");
            }

            sb.append(" Action: ").append(action);
            return sb.toString();
        }

        @Override
        public String toString() {
            return message;
        }
    }

    // ===== ErrorHandler fields =====

    private List<SyntaxError> errors;                      // All errors collected
    private Map<String, Set<String>> followSets;           // FOLLOW sets for panic-mode recovery
    private Set<String> terminals;                          // Terminal symbols of the grammar

    /**
     * Creates an error handler with the given FOLLOW sets and terminal set.
     *
     * @param followSets FOLLOW sets for all non-terminals
     * @param terminals  set of terminal symbols
     */
    public ErrorHandler(Map<String, Set<String>> followSets, Set<String> terminals) {
        this.errors = new ArrayList<>();
        this.followSets = followSets;
        this.terminals = terminals;
    }

    /**
     * Reports and records a syntax error.
     *
     * @param error the SyntaxError to record
     */
    public void reportError(SyntaxError error) {
        errors.add(error);
        System.err.println("  >> " + error.message);
    }

    /**
     * Creates and reports a MISSING_SYMBOL error.
     * Used when the top of stack is a terminal that doesn't match input.
     *
     * @param line     line number
     * @param col      column number
     * @param expected the expected terminal
     * @param found    the actual input symbol
     * @return the created SyntaxError
     */
    public SyntaxError missingSymbol(int line, int col, String expected, String found) {
        SyntaxError err = new SyntaxError(line, col, "MISSING_SYMBOL",
                expected, found, "Popped '" + expected + "' from stack (inserted)");
        reportError(err);
        return err;
    }

    /**
     * Creates and reports an UNEXPECTED_SYMBOL error (empty table entry).
     * Used in panic-mode recovery when M[X, a] is empty.
     *
     * @param line        line number
     * @param col         column number
     * @param nonTerminal the non-terminal on top of stack
     * @param found       the current input symbol
     * @param action      description of recovery action taken
     * @return the created SyntaxError
     */
    public SyntaxError unexpectedSymbol(int line, int col, String nonTerminal,
                                         String found, String action) {
        // Build expected terminals list from FOLLOW set
        Set<String> follow = followSets.getOrDefault(nonTerminal, new LinkedHashSet<>());
        String expectedStr = nonTerminal + " expects: " + String.join(", ", follow);

        SyntaxError err = new SyntaxError(line, col, "EMPTY_TABLE_ENTRY",
                nonTerminal, found, action);
        reportError(err);
        return err;
    }

    /**
     * Creates and reports a PREMATURE_END error.
     * Used when input ends but stack still has symbols.
     *
     * @param line     line number
     * @param col      column number
     * @param expected the symbol still on the stack
     * @return the created SyntaxError
     */
    public SyntaxError prematureEnd(int line, int col, String expected) {
        SyntaxError err = new SyntaxError(line, col, "PREMATURE_END",
                expected, "$", "Popped remaining stack symbols");
        reportError(err);
        return err;
    }

    /**
     * Performs panic-mode recovery for an empty table entry M[nonTerminal, inputSymbol].
     * Strategy:
     *   1. If the input symbol is in FOLLOW(nonTerminal), pop the non-terminal
     *      (treat it as producing epsilon).
     *   2. Otherwise, skip the input symbol and try again.
     *
     * @param nonTerminal the non-terminal on top of stack
     * @param inputSymbol the current input symbol
     * @return "POP" if the non-terminal should be popped, "SKIP" if input should be skipped
     */
    public String panicModeRecover(String nonTerminal, String inputSymbol) {
        Set<String> follow = followSets.getOrDefault(nonTerminal, new LinkedHashSet<>());

        if (follow.contains(inputSymbol)) {
            // Input symbol is a synchronizing symbol — pop the non-terminal
            return "POP";
        } else {
            // Skip the input symbol
            return "SKIP";
        }
    }

    /**
     * Returns whether any errors were detected.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the total number of errors detected.
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the list of all errors.
     */
    public List<SyntaxError> getErrors() {
        return errors;
    }

    /**
     * Displays all errors to the console.
     */
    public void displayErrors() {
        if (errors.isEmpty()) {
            System.out.println("  No syntax errors detected.");
            return;
        }

        System.out.println("\n  Syntax Errors Summary (" + errors.size() + " error(s)):");
        System.out.println("  " + "-".repeat(58));
        for (int i = 0; i < errors.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + errors.get(i).message);
        }
    }

    /**
     * Writes the error summary to an output file.
     *
     * @param filePath the output file path
     */
    public void writeErrorsToFile(String filePath) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));
        if (errors.isEmpty()) {
            writer.println("  No syntax errors detected.");
        } else {
            writer.println("\n  Syntax Errors Summary (" + errors.size() + " error(s)):");
            writer.println("  " + "-".repeat(58));
            for (int i = 0; i < errors.size(); i++) {
                writer.println("  " + (i + 1) + ". " + errors.get(i).message);
            }
        }
        writer.close();
    }
}
