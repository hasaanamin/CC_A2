/*
 * Grammar.java
 * CS4031 - Compiler Construction, Assignment 02
 * LL(1) Parser Design & Implementation - Spring 2026
 *
 * Group Members:
 *   - [RollNumber1] [Name1]
 *   - [RollNumber2] [Name2]
 * Section: [Section]
 *
 * Description:
 *   Represents a Context-Free Grammar (CFG). Handles parsing from file,
 *   display, deep copy, and utility methods for grammar transformations.
 */

import java.io.*;
import java.util.*;

public class Grammar {

    // Ordered list of non-terminals (preserves definition order)
    private List<String> nonTerminals;

    // Map: NonTerminal -> list of alternatives, each alternative is a list of symbols
    private Map<String, List<List<String>>> productions;

    // The start symbol (first non-terminal defined)
    private String startSymbol;

    // Set of all terminals found in the grammar
    private Set<String> terminals;

    // Counter for generating unique new non-terminal names
    private int nameCounter = 0;

    /**
     * Default constructor - creates an empty grammar.
     */
    public Grammar() {
        this.nonTerminals = new ArrayList<>();
        this.productions = new LinkedHashMap<>();
        this.terminals = new LinkedHashSet<>();
        this.startSymbol = null;
    }

    /**
     * Loads a CFG from a text file.
     * Format: NonTerminal -> production1 | production2 | ...
     * Symbols within each production are separated by whitespace.
     * Epsilon is represented as "epsilon" or "@".
     *
     * @param filePath path to the grammar file
     * @return a Grammar object representing the parsed CFG
     * @throws IOException if file reading fails
     */
    public static Grammar loadFromFile(String filePath) throws IOException {
        Grammar grammar = new Grammar();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int lineNum = 0;

        while ((line = reader.readLine()) != null) {
            lineNum++;
            line = line.trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                continue;
            }

            // Split on "->"
            String[] parts = line.split("->");
            if (parts.length != 2) {
                System.err.println("Warning: Skipping malformed line " + lineNum + ": " + line);
                continue;
            }

            String lhs = parts[0].trim();

            // Validate non-terminal: must start with uppercase, multi-character encouraged
            if (lhs.isEmpty() || !Character.isUpperCase(lhs.charAt(0))) {
                System.err.println("Warning: Invalid non-terminal '" + lhs + "' at line " + lineNum);
                continue;
            }

            // Warn about single-character non-terminals
            if (lhs.length() == 1) {
                System.err.println("Warning: Single-character non-terminal '" + lhs
                        + "' at line " + lineNum + " (assignment says NOT allowed).");
            }

            // Parse the right-hand side alternatives
            String rhs = parts[1].trim();
            String[] alternatives = rhs.split("\\|");

            List<List<String>> altList = new ArrayList<>();
            if (grammar.productions.containsKey(lhs)) {
                // If this non-terminal already has productions, append to them
                altList = grammar.productions.get(lhs);
            } else {
                // Register new non-terminal
                grammar.nonTerminals.add(lhs);
            }

            for (String alt : alternatives) {
                alt = alt.trim();
                if (alt.isEmpty()) continue;

                String[] symbols = alt.split("\\s+");
                List<String> symbolList = new ArrayList<>();

                for (String sym : symbols) {
                    // Normalize epsilon representations
                    if (sym.equalsIgnoreCase("epsilon")) {
                        sym = "@";
                    }
                    symbolList.add(sym);
                }

                altList.add(symbolList);
            }

            grammar.productions.put(lhs, altList);
        }

        reader.close();

        // Set start symbol as the first non-terminal defined
        if (!grammar.nonTerminals.isEmpty()) {
            grammar.startSymbol = grammar.nonTerminals.get(0);
        }

        // Collect all terminals
        grammar.collectTerminals();

        return grammar;
    }

    /**
     * Scans all productions to identify terminal symbols.
     * A terminal is any symbol that is NOT a non-terminal and is NOT epsilon (@).
     */
    public void collectTerminals() {
        terminals.clear();
        for (String nt : nonTerminals) {
            List<List<String>> alts = productions.get(nt);
            if (alts == null) continue;
            for (List<String> alt : alts) {
                for (String sym : alt) {
                    if (!isNonTerminal(sym) && !sym.equals("@")) {
                        terminals.add(sym);
                    }
                }
            }
        }
    }

    /**
     * Checks if a symbol is a non-terminal.
     * Non-terminals start with an uppercase letter.
     */
    public boolean isNonTerminal(String symbol) {
        return symbol != null && !symbol.isEmpty()
                && Character.isUpperCase(symbol.charAt(0))
                && nonTerminals.contains(symbol);
    }

    /**
     * Checks if a symbol is a terminal (not a non-terminal, not epsilon).
     */
    public boolean isTerminal(String symbol) {
        return symbol != null && !symbol.equals("@") && !isNonTerminal(symbol);
    }

    /**
     * Creates a deep copy of this grammar.
     */
    public Grammar deepCopy() {
        Grammar copy = new Grammar();
        copy.startSymbol = this.startSymbol;
        copy.nameCounter = this.nameCounter;
        copy.nonTerminals = new ArrayList<>(this.nonTerminals);
        copy.terminals = new LinkedHashSet<>(this.terminals);

        for (Map.Entry<String, List<List<String>>> entry : this.productions.entrySet()) {
            List<List<String>> altsCopy = new ArrayList<>();
            for (List<String> alt : entry.getValue()) {
                altsCopy.add(new ArrayList<>(alt));
            }
            copy.productions.put(entry.getKey(), altsCopy);
        }

        return copy;
    }

    /**
     * Generates a unique new non-terminal name based on a base name.
     * Uses "Prime" suffix: ExprPrime, ExprPrime2, etc.
     */
    public String generateNewName(String base) {
        // Remove existing "Prime" suffix to avoid ExprPrimePrime
        String cleanBase = base;
        while (cleanBase.endsWith("Prime")) {
            cleanBase = cleanBase.substring(0, cleanBase.length() - 5);
        }

        String candidate = cleanBase + "Prime";
        while (nonTerminals.contains(candidate)) {
            nameCounter++;
            candidate = cleanBase + "Prime" + nameCounter;
        }
        return candidate;
    }

    /**
     * Adds a new non-terminal to the grammar (if not already present).
     * Inserts it right after the specified 'afterNt' in the ordered list.
     */
    public void addNonTerminal(String nt, String afterNt) {
        if (!nonTerminals.contains(nt)) {
            int idx = nonTerminals.indexOf(afterNt);
            if (idx >= 0) {
                nonTerminals.add(idx + 1, nt);
            } else {
                nonTerminals.add(nt);
            }
        }
        if (!productions.containsKey(nt)) {
            productions.put(nt, new ArrayList<>());
        }
    }

    /**
     * Displays the grammar in a readable format.
     */
    public void display(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));

        for (String nt : nonTerminals) {
            List<List<String>> alts = productions.get(nt);
            if (alts == null || alts.isEmpty()) continue;

            StringBuilder sb = new StringBuilder();
            sb.append("  ").append(nt).append(" -> ");

            for (int i = 0; i < alts.size(); i++) {
                if (i > 0) sb.append(" | ");
                List<String> alt = alts.get(i);
                sb.append(String.join(" ", alt));
            }

            System.out.println(sb.toString());
        }

        System.out.println("=".repeat(60));
    }

    /**
     * Writes the grammar to an output file.
     */
    public void writeToFile(String filePath, String title) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));
        writer.println("\n" + "=".repeat(60));
        writer.println("  " + title);
        writer.println("=".repeat(60));

        for (String nt : nonTerminals) {
            List<List<String>> alts = productions.get(nt);
            if (alts == null || alts.isEmpty()) continue;

            StringBuilder sb = new StringBuilder();
            sb.append("  ").append(nt).append(" -> ");

            for (int i = 0; i < alts.size(); i++) {
                if (i > 0) sb.append(" | ");
                List<String> alt = alts.get(i);
                sb.append(String.join(" ", alt));
            }

            writer.println(sb.toString());
        }

        writer.println("=".repeat(60));
        writer.close();
    }

    // ===== Getters and Setters =====

    public String getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(String startSymbol) {
        this.startSymbol = startSymbol;
    }

    public List<String> getNonTerminals() {
        return nonTerminals;
    }

    public Set<String> getTerminals() {
        collectTerminals();
        return terminals;
    }

    public List<List<String>> getProductions(String nt) {
        return productions.getOrDefault(nt, new ArrayList<>());
    }

    public void setProductions(String nt, List<List<String>> alts) {
        productions.put(nt, alts);
    }

    public Map<String, List<List<String>>> getAllProductions() {
        return productions;
    }

    /**
     * Removes a non-terminal and its productions entirely.
     */
    public void removeNonTerminal(String nt) {
        nonTerminals.remove(nt);
        productions.remove(nt);
    }
}
