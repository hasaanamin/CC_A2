/*
 * FirstFollow.java
 * CS4031 - Compiler Construction, Assignment 02
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Computes FIRST and FOLLOW sets for all non-terminals in a CFG.
 *   Uses fixed-point iteration (repeats until no changes).
 *   Also provides utility to compute FIRST of a sequence of symbols.
 */

import java.io.*;
import java.util.*;

public class FirstFollow {

    /**
     * Computes FIRST sets for all non-terminals in the grammar.
     * Uses fixed-point iteration.
     *
     * Rules:
     *   1. If X is a terminal, FIRST(X) = {X}
     *   2. If X -> @ exists, add @ to FIRST(X)
     *   3. If X -> Y1 Y2 ... Yk:
     *      - Add FIRST(Y1) - {@} to FIRST(X)
     *      - If @ in FIRST(Y1), add FIRST(Y2) - {@}, ...
     *      - If @ in FIRST(Yi) for all i=1..k, add @ to FIRST(X)
     *
     * @param grammar the input grammar
     * @return map from non-terminal to its FIRST set
     */
    public static Map<String, Set<String>> computeFirst(Grammar grammar) {
        Map<String, Set<String>> firstSets = new LinkedHashMap<>();

        // Initialize empty FIRST sets for all non-terminals
        for (String nt : grammar.getNonTerminals()) {
            firstSets.put(nt, new LinkedHashSet<>());
        }

        boolean changed = true;

        // Fixed-point iteration
        while (changed) {
            changed = false;

            for (String nt : grammar.getNonTerminals()) {
                Set<String> firstSet = firstSets.get(nt);
                List<List<String>> alts = grammar.getProductions(nt);

                for (List<String> alt : alts) {
                    // Compute FIRST of this alternative and add to FIRST(nt)
                    Set<String> altFirst = firstOfSequence(alt, firstSets, grammar);

                    for (String sym : altFirst) {
                        if (firstSet.add(sym)) {
                            changed = true;
                        }
                    }
                }
            }
        }

        return firstSets;
    }

    /**
     * Computes FOLLOW sets for all non-terminals in the grammar.
     * Uses fixed-point iteration.
     *
     * Rules:
     *   1. Add $ to FOLLOW(start symbol)
     *   2. For production A -> alpha B beta:
     *      - Add FIRST(beta) - {@} to FOLLOW(B)
     *      - If @ in FIRST(beta) or beta is empty, add FOLLOW(A) to FOLLOW(B)
     *   3. Repeat until no changes
     *
     * @param grammar   the input grammar
     * @param firstSets the precomputed FIRST sets
     * @return map from non-terminal to its FOLLOW set
     */
    public static Map<String, Set<String>> computeFollow(Grammar grammar,
                                                          Map<String, Set<String>> firstSets) {
        Map<String, Set<String>> followSets = new LinkedHashMap<>();

        // Initialize empty FOLLOW sets for all non-terminals
        for (String nt : grammar.getNonTerminals()) {
            followSets.put(nt, new LinkedHashSet<>());
        }

        // Rule 1: Add $ to FOLLOW(start symbol)
        followSets.get(grammar.getStartSymbol()).add("$");

        boolean changed = true;

        // Fixed-point iteration
        while (changed) {
            changed = false;

            for (String nt : grammar.getNonTerminals()) {
                List<List<String>> alts = grammar.getProductions(nt);

                for (List<String> alt : alts) {
                    for (int i = 0; i < alt.size(); i++) {
                        String symbol = alt.get(i);

                        // Only process non-terminals
                        if (!grammar.isNonTerminal(symbol)) continue;

                        // beta = everything after this symbol
                        List<String> beta = alt.subList(i + 1, alt.size());

                        // Compute FIRST(beta)
                        Set<String> firstBeta = firstOfSequence(beta, firstSets, grammar);

                        Set<String> followB = followSets.get(symbol);

                        // Add FIRST(beta) - {@} to FOLLOW(B)
                        for (String sym : firstBeta) {
                            if (!sym.equals("@")) {
                                if (followB.add(sym)) {
                                    changed = true;
                                }
                            }
                        }

                        // If @ in FIRST(beta) or beta is empty, add FOLLOW(A) to FOLLOW(B)
                        if (firstBeta.contains("@") || beta.isEmpty()) {
                            Set<String> followA = followSets.get(nt);
                            for (String sym : followA) {
                                if (followB.add(sym)) {
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return followSets;
    }

    /**
     * Computes FIRST of a sequence of symbols (used for parsing table construction).
     *
     * FIRST(Y1 Y2 ... Yk):
     *   - Start with FIRST(Y1) - {@}
     *   - If @ in FIRST(Y1), add FIRST(Y2) - {@}, and so on
     *   - If @ in FIRST(Yi) for all i, add @
     *
     * @param symbols   the sequence of symbols
     * @param firstSets the precomputed FIRST sets for non-terminals
     * @param grammar   the grammar (to determine terminal vs non-terminal)
     * @return the FIRST set of the sequence
     */
    public static Set<String> firstOfSequence(List<String> symbols,
                                               Map<String, Set<String>> firstSets,
                                               Grammar grammar) {
        Set<String> result = new LinkedHashSet<>();

        // Empty sequence or just epsilon
        if (symbols.isEmpty()) {
            result.add("@");
            return result;
        }

        boolean allCanDeriveEpsilon = true;

        for (String sym : symbols) {
            if (sym.equals("@")) {
                // Epsilon symbol — skip, it means this position can be empty
                continue;
            }

            if (grammar.isTerminal(sym) || (!grammar.isNonTerminal(sym) && !sym.equals("@"))) {
                // Terminal: FIRST(terminal) = {terminal}
                result.add(sym);
                allCanDeriveEpsilon = false;
                break;
            }

            if (grammar.isNonTerminal(sym)) {
                Set<String> firstOfSym = firstSets.getOrDefault(sym, new LinkedHashSet<>());

                // Add FIRST(sym) - {@}
                for (String f : firstOfSym) {
                    if (!f.equals("@")) {
                        result.add(f);
                    }
                }

                // If epsilon is NOT in FIRST(sym), stop
                if (!firstOfSym.contains("@")) {
                    allCanDeriveEpsilon = false;
                    break;
                }
                // Otherwise continue to next symbol
            }
        }

        // If all symbols can derive epsilon, add epsilon to the result
        if (allCanDeriveEpsilon) {
            result.add("@");
        }

        return result;
    }

    /**
     * Displays FIRST or FOLLOW sets in a formatted table.
     *
     * @param title the title to display
     * @param sets  the sets to display
     */
    public static void displaySets(String title, Map<String, Set<String>> sets) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
        System.out.printf("  %-20s %s%n", "Non-Terminal", "Set");
        System.out.println("  " + "-".repeat(50));

        for (Map.Entry<String, Set<String>> entry : sets.entrySet()) {
            String nt = entry.getKey();
            Set<String> set = entry.getValue();
            System.out.printf("  %-20s { %s }%n", nt, String.join(", ", set));
        }

        System.out.println("=".repeat(60));
    }

    /**
     * Writes FIRST or FOLLOW sets to an output file in tabular format.
     *
     * @param filePath the output file path
     * @param title    the title
     * @param sets     the sets to write
     */
    public static void writeSetsToFile(String filePath, String title,
                                        Map<String, Set<String>> sets) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));
        writer.println("\n" + "=".repeat(60));
        writer.println("  " + title);
        writer.println("=".repeat(60));
        writer.printf("  %-20s %s%n", "Non-Terminal", "Set");
        writer.println("  " + "-".repeat(50));

        for (Map.Entry<String, Set<String>> entry : sets.entrySet()) {
            String nt = entry.getKey();
            Set<String> set = entry.getValue();
            writer.printf("  %-20s { %s }%n", nt, String.join(", ", set));
        }

        writer.println("=".repeat(60));
        writer.close();
    }
}

