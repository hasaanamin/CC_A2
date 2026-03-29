/*
 * ParsingTable.java
 * CS4031 - Compiler Construction, Assignment 02
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Constructs the LL(1) parsing table M[A, a] from FIRST and FOLLOW sets.
 *   Detects conflicts (multiple entries in a cell) to determine if the
 *   grammar is LL(1) or not.
 */

import java.io.*;
import java.util.*;

public class ParsingTable {

    // The parsing table: M[NonTerminal][Terminal] -> list of productions
    // Each entry is a list to handle conflicts (multiple entries = not LL(1))
    private Map<String, Map<String, List<String>>> table;

    // All terminals (including $) used as column headers
    private List<String> terminalsList;

    // All non-terminals used as row headers
    private List<String> nonTerminalsList;

    // Whether the grammar is LL(1)
    private boolean isLL1;

    /**
     * Constructs the LL(1) parsing table.
     *
     * Algorithm:
     *   For each production A -> alpha:
     *     1. For each terminal 'a' in FIRST(alpha): add "A -> alpha" to M[A, a]
     *     2. If @ in FIRST(alpha):
     *        - For each terminal 'b' in FOLLOW(A): add "A -> alpha" to M[A, b]
     *        - If $ in FOLLOW(A): add "A -> alpha" to M[A, $]
     *
     * @param grammar    the transformed grammar
     * @param firstSets  FIRST sets for all non-terminals
     * @param followSets FOLLOW sets for all non-terminals
     * @return the constructed ParsingTable
     */
    public static ParsingTable construct(Grammar grammar,
                                          Map<String, Set<String>> firstSets,
                                          Map<String, Set<String>> followSets) {
        ParsingTable pt = new ParsingTable();

        pt.nonTerminalsList = new ArrayList<>(grammar.getNonTerminals());

        // Collect all terminals + $
        pt.terminalsList = new ArrayList<>(grammar.getTerminals());
        if (!pt.terminalsList.contains("$")) {
            pt.terminalsList.add("$");
        }

        // Initialize the table with empty lists
        pt.table = new LinkedHashMap<>();
        for (String nt : pt.nonTerminalsList) {
            Map<String, List<String>> row = new LinkedHashMap<>();
            for (String t : pt.terminalsList) {
                row.put(t, new ArrayList<>());
            }
            pt.table.put(nt, row);
        }

        // Fill the table
        for (String nt : pt.nonTerminalsList) {
            List<List<String>> alts = grammar.getProductions(nt);

            for (List<String> alt : alts) {
                // Format the production string: "A -> sym1 sym2 ..."
                String prodStr = nt + " -> " + String.join(" ", alt);

                // Compute FIRST of this alternative
                Set<String> firstAlpha = FirstFollow.firstOfSequence(alt, firstSets, grammar);

                // Rule 1: For each terminal in FIRST(alpha), add to M[A, a]
                for (String sym : firstAlpha) {
                    if (!sym.equals("@") && pt.table.get(nt).containsKey(sym)) {
                        pt.table.get(nt).get(sym).add(prodStr);
                    }
                }

                // Rule 2: If epsilon is in FIRST(alpha), use FOLLOW set
                if (firstAlpha.contains("@")) {
                    Set<String> followA = followSets.getOrDefault(nt, new LinkedHashSet<>());

                    for (String sym : followA) {
                        if (pt.table.get(nt).containsKey(sym)) {
                            pt.table.get(nt).get(sym).add(prodStr);
                        }
                    }
                }
            }
        }

        // Check if the grammar is LL(1)
        pt.isLL1 = true;
        for (String nt : pt.nonTerminalsList) {
            for (String t : pt.terminalsList) {
                List<String> entries = pt.table.get(nt).get(t);
                if (entries.size() > 1) {
                    pt.isLL1 = false;
                    break;
                }
            }
            if (!pt.isLL1) break;
        }

        return pt;
    }

    /**
     * Returns whether the grammar is LL(1).
     */
    public boolean isLL1() {
        return isLL1;
    }

    /**
     * Returns the parsing table.
     */
    public Map<String, Map<String, List<String>>> getTable() {
        return table;
    }

    /**
     * Returns the list of terminals (column headers).
     */
    public List<String> getTerminals() {
        return terminalsList;
    }

    /**
     * Returns the list of non-terminals (row headers).
     */
    public List<String> getNonTerminals() {
        return nonTerminalsList;
    }

    /**
     * Displays the LL(1) parsing table in a formatted manner.
     */
    public void display() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  LL(1) Parsing Table");
        System.out.println("=".repeat(80));

        // Determine column widths
        int ntWidth = 15;
        for (String nt : nonTerminalsList) {
            ntWidth = Math.max(ntWidth, nt.length() + 2);
        }

        int colWidth = 20;
        for (String nt : nonTerminalsList) {
            for (String t : terminalsList) {
                List<String> entries = table.get(nt).get(t);
                if (!entries.isEmpty()) {
                    String cellContent = String.join("; ", entries);
                    colWidth = Math.max(colWidth, cellContent.length() + 2);
                }
            }
        }
        colWidth = Math.min(colWidth, 35); // Cap column width

        // Print header row
        System.out.printf("  %-" + ntWidth + "s", "");
        for (String t : terminalsList) {
            System.out.printf("| %-" + colWidth + "s", t);
        }
        System.out.println("|");

        // Print separator
        System.out.print("  " + "-".repeat(ntWidth));
        for (int i = 0; i < terminalsList.size(); i++) {
            System.out.print("+" + "-".repeat(colWidth + 1));
        }
        System.out.println("+");

        // Print each row
        for (String nt : nonTerminalsList) {
            System.out.printf("  %-" + ntWidth + "s", nt);
            for (String t : terminalsList) {
                List<String> entries = table.get(nt).get(t);
                String cellContent = entries.isEmpty() ? "" : String.join("; ", entries);

                // Truncate if too long
                if (cellContent.length() > colWidth) {
                    cellContent = cellContent.substring(0, colWidth - 3) + "...";
                }

                System.out.printf("| %-" + colWidth + "s", cellContent);
            }
            System.out.println("|");
        }

        System.out.println("=".repeat(80));

        // Print LL(1) status
        if (isLL1) {
            System.out.println("\n  [OK] The grammar IS LL(1).");
        } else {
            System.out.println("\n  [CONFLICT] The grammar is NOT LL(1).");
            System.out.println("  Cells with multiple entries (conflicts):");

            for (String nt : nonTerminalsList) {
                for (String t : terminalsList) {
                    List<String> entries = table.get(nt).get(t);
                    if (entries.size() > 1) {
                        System.out.println("    M[" + nt + ", " + t + "] = {");
                        for (String e : entries) {
                            System.out.println("      " + e);
                        }
                        System.out.println("    }");
                    }
                }
            }
        }
    }

    /**
     * Writes the parsing table to an output file.
     */
    public void writeToFile(String filePath) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));

        writer.println("\n" + "=".repeat(80));
        writer.println("  LL(1) Parsing Table");
        writer.println("=".repeat(80));

        int ntWidth = 15;
        for (String nt : nonTerminalsList) {
            ntWidth = Math.max(ntWidth, nt.length() + 2);
        }

        int colWidth = 20;
        for (String nt : nonTerminalsList) {
            for (String t : terminalsList) {
                List<String> entries = table.get(nt).get(t);
                if (!entries.isEmpty()) {
                    String cellContent = String.join("; ", entries);
                    colWidth = Math.max(colWidth, cellContent.length() + 2);
                }
            }
        }
        colWidth = Math.min(colWidth, 35);

        // Header
        writer.printf("  %-" + ntWidth + "s", "");
        for (String t : terminalsList) {
            writer.printf("| %-" + colWidth + "s", t);
        }
        writer.println("|");

        // Separator
        writer.print("  " + "-".repeat(ntWidth));
        for (int i = 0; i < terminalsList.size(); i++) {
            writer.print("+" + "-".repeat(colWidth + 1));
        }
        writer.println("+");

        // Rows
        for (String nt : nonTerminalsList) {
            writer.printf("  %-" + ntWidth + "s", nt);
            for (String t : terminalsList) {
                List<String> entries = table.get(nt).get(t);
                String cellContent = entries.isEmpty() ? "" : String.join("; ", entries);
                if (cellContent.length() > colWidth) {
                    cellContent = cellContent.substring(0, colWidth - 3) + "...";
                }
                writer.printf("| %-" + colWidth + "s", cellContent);
            }
            writer.println("|");
        }

        writer.println("=".repeat(80));

        // LL(1) status
        if (isLL1) {
            writer.println("\n  [OK] The grammar IS LL(1).");
        } else {
            writer.println("\n  [CONFLICT] The grammar is NOT LL(1).");
            writer.println("  Cells with multiple entries (conflicts):");
            for (String nt : nonTerminalsList) {
                for (String t : terminalsList) {
                    List<String> entries = table.get(nt).get(t);
                    if (entries.size() > 1) {
                        writer.println("    M[" + nt + ", " + t + "] = {");
                        for (String e : entries) {
                            writer.println("      " + e);
                        }
                        writer.println("    }");
                    }
                }
            }
        }

        writer.close();
    }
}

