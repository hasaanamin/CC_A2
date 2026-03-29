/*
 * LeftFactoring.java
 * CS4031 - Compiler Construction, Assignment 02
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Implements left factoring transformation on a CFG.
 *   Eliminates common prefixes by introducing new non-terminals.
 *   Runs to a fixed-point (repeats until no more factoring is possible).
 */

import java.util.*;

public class LeftFactoring {

    /**
     * Applies left factoring to the given grammar.
     * Returns a new grammar with common prefixes eliminated.
     *
     * @param grammar the input grammar (will not be modified)
     * @return a new Grammar with left factoring applied
     */
    public static Grammar apply(Grammar grammar) {
        Grammar g = grammar.deepCopy();
        boolean changed = true;

        // Repeat until no more factoring is possible (fixed-point)
        while (changed) {
            changed = false;

            // Work on a snapshot of the current non-terminals list
            List<String> currentNTs = new ArrayList<>(g.getNonTerminals());

            for (String nt : currentNTs) {
                List<List<String>> alts = g.getProductions(nt);
                if (alts == null || alts.size() <= 1) continue;

                // Try to find and factor a common prefix
                boolean factored = factorNonTerminal(g, nt);
                if (factored) {
                    changed = true;
                }
            }
        }

        // Refresh terminal set after transformations
        g.collectTerminals();
        return g;
    }

    /**
     * Attempts to left-factor productions of a single non-terminal.
     *
     * @param g  the grammar being modified
     * @param nt the non-terminal to factor
     * @return true if any factoring was performed
     */
    private static boolean factorNonTerminal(Grammar g, String nt) {
        List<List<String>> alts = g.getProductions(nt);
        if (alts == null || alts.size() <= 1) return false;

        // Group alternatives by their first symbol
        Map<String, List<List<String>>> groups = new LinkedHashMap<>();
        for (List<String> alt : alts) {
            String firstSym = alt.get(0);
            groups.computeIfAbsent(firstSym, k -> new ArrayList<>()).add(alt);
        }

        // Find any group with more than one alternative (common prefix exists)
        for (Map.Entry<String, List<List<String>>> entry : groups.entrySet()) {
            List<List<String>> group = entry.getValue();
            if (group.size() <= 1) continue;

            // Find the longest common prefix in this group
            List<String> prefix = findLongestCommonPrefix(group);
            if (prefix.isEmpty()) continue;

            // Generate a new non-terminal for the suffixes
            String newNT = g.generateNewName(nt);
            g.addNonTerminal(newNT, nt);

            // Build the new alternatives for the original non-terminal
            List<List<String>> newAlts = new ArrayList<>();

            // Add the factored production: prefix + NewNT
            List<String> factoredAlt = new ArrayList<>(prefix);
            factoredAlt.add(newNT);
            newAlts.add(factoredAlt);

            // Add all alternatives that don't share this prefix (unchanged)
            for (List<String> alt : alts) {
                if (!group.contains(alt)) {
                    newAlts.add(new ArrayList<>(alt));
                }
            }

            g.setProductions(nt, newAlts);

            // Build productions for the new non-terminal (suffixes after prefix)
            List<List<String>> suffixAlts = new ArrayList<>();
            for (List<String> alt : group) {
                List<String> suffix = new ArrayList<>(alt.subList(prefix.size(), alt.size()));
                if (suffix.isEmpty()) {
                    // Empty suffix means epsilon
                    suffix.add("@");
                }
                suffixAlts.add(suffix);
            }

            g.setProductions(newNT, suffixAlts);

            return true; // One factoring step done, restart the loop
        }

        return false;
    }

    /**
     * Finds the longest common prefix among a list of alternatives.
     *
     * @param alts the alternatives to compare
     * @return the longest common prefix as a list of symbols
     */
    private static List<String> findLongestCommonPrefix(List<List<String>> alts) {
        if (alts == null || alts.isEmpty()) return new ArrayList<>();

        List<String> prefix = new ArrayList<>();
        List<String> first = alts.get(0);

        for (int i = 0; i < first.size(); i++) {
            String sym = first.get(i);
            boolean allMatch = true;

            for (int j = 1; j < alts.size(); j++) {
                List<String> other = alts.get(j);
                if (i >= other.size() || !other.get(i).equals(sym)) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                prefix.add(sym);
            } else {
                break;
            }
        }

        return prefix;
    }
}

