/*
 * LeftRecursion.java
 * CS4031 - Compiler Construction, Assignment 02
 *
 * Description:
 *   Removes both direct and indirect left recursion from a CFG.
 *   Uses the standard algorithm:
 *     1. Order non-terminals A1, A2, ..., An
 *     2. For i = 1 to n:
 *        - For j = 1 to i-1: substitute Aj productions into Ai
 *        - Remove direct left recursion from Ai
 */

import java.util.*;

public class LeftRecursion {

    /**
     * Applies left recursion removal (both direct and indirect) to the grammar.
     *
     * @param grammar the input grammar (will not be modified)
     * @return a new Grammar with all left recursion removed
     */
    public static Grammar apply(Grammar grammar) {
        Grammar g = grammar.deepCopy();
        List<String> orderedNTs = new ArrayList<>(g.getNonTerminals());

        // Standard algorithm for eliminating indirect + direct left recursion
        for (int i = 0; i < orderedNTs.size(); i++) {
            String ai = orderedNTs.get(i);

            // Step 1: Substitute productions of earlier non-terminals
            for (int j = 0; j < i; j++) {
                String aj = orderedNTs.get(j);
                substituteProductions(g, ai, aj);
            }

            // Step 2: Remove direct left recursion from Ai
            removeDirectLeftRecursion(g, ai);
        }

        // Clean up: remove any non-terminals with no productions
        List<String> toRemove = new ArrayList<>();
        for (String nt : g.getNonTerminals()) {
            List<List<String>> alts = g.getProductions(nt);
            if (alts == null || alts.isEmpty()) {
                toRemove.add(nt);
            }
        }
        for (String nt : toRemove) {
            g.removeNonTerminal(nt);
        }

        // Refresh terminal set
        g.collectTerminals();
        return g;
    }

    /**
     * Substitutes: For each production Ai -> Aj gamma,
     * replace it with Ai -> delta1 gamma | delta2 gamma | ...
     * where Aj -> delta1 | delta2 | ...
     *
     * @param g  the grammar being modified
     * @param ai the non-terminal being processed
     * @param aj the earlier non-terminal to substitute
     */
    private static void substituteProductions(Grammar g, String ai, String aj) {
        List<List<String>> aiAlts = g.getProductions(ai);
        if (aiAlts == null) return;

        List<List<String>> ajAlts = g.getProductions(aj);
        if (ajAlts == null || ajAlts.isEmpty()) return;

        List<List<String>> newAlts = new ArrayList<>();

        for (List<String> alt : aiAlts) {
            if (!alt.isEmpty() && alt.get(0).equals(aj)) {
                // This production starts with Aj — substitute
                List<String> gamma = new ArrayList<>(alt.subList(1, alt.size()));

                for (List<String> delta : ajAlts) {
                    List<String> newAlt = new ArrayList<>();
                    // Handle the case where delta is just epsilon
                    if (delta.size() == 1 && delta.get(0).equals("@")) {
                        if (gamma.isEmpty()) {
                            newAlt.add("@");
                        } else {
                            newAlt.addAll(gamma);
                        }
                    } else {
                        newAlt.addAll(delta);
                        newAlt.addAll(gamma);
                    }
                    newAlts.add(newAlt);
                }
            } else {
                // Not starting with Aj — keep as-is
                newAlts.add(new ArrayList<>(alt));
            }
        }

        g.setProductions(ai, newAlts);
    }

    /**
     * Removes direct left recursion from a single non-terminal.
     *
     * Given: A -> A alpha1 | A alpha2 | ... | beta1 | beta2 | ...
     * Produces:
     *   A -> beta1 APrime | beta2 APrime | ...
     *   APrime -> alpha1 APrime | alpha2 APrime | ... | @
     *
     * @param g  the grammar being modified
     * @param nt the non-terminal to process
     */
    private static void removeDirectLeftRecursion(Grammar g, String nt) {
        List<List<String>> alts = g.getProductions(nt);
        if (alts == null || alts.isEmpty()) return;

        // Separate into left-recursive (alpha) and non-left-recursive (beta) alternatives
        List<List<String>> alphas = new ArrayList<>(); // A -> A alpha (store just alpha)
        List<List<String>> betas = new ArrayList<>();   // A -> beta

        for (List<String> alt : alts) {
            if (!alt.isEmpty() && alt.get(0).equals(nt)) {
                // Left-recursive: A -> A alpha
                List<String> alpha = new ArrayList<>(alt.subList(1, alt.size()));
                if (alpha.isEmpty()) {
                    // A -> A (infinite loop, skip)
                    System.err.println("Warning: Production " + nt + " -> " + nt
                            + " causes infinite loop, skipping.");
                    continue;
                }
                alphas.add(alpha);
            } else {
                // Non-left-recursive
                betas.add(new ArrayList<>(alt));
            }
        }

        // If no left recursion found, nothing to do
        if (alphas.isEmpty()) return;

        // If no non-recursive alternatives, we have a problem
        if (betas.isEmpty()) {
            System.err.println("Error: Non-terminal '" + nt
                    + "' has only left-recursive productions!");
            return;
        }

        // Generate new non-terminal name: e.g., ExprPrime
        String newNT = g.generateNewName(nt);
        g.addNonTerminal(newNT, nt);

        // Build new productions for the original non-terminal
        // A -> beta1 APrime | beta2 APrime | ...
        List<List<String>> newAlts = new ArrayList<>();
        for (List<String> beta : betas) {
            List<String> newAlt = new ArrayList<>();
            if (beta.size() == 1 && beta.get(0).equals("@")) {
                // A -> epsilon becomes A -> APrime
                newAlt.add(newNT);
            } else {
                newAlt.addAll(beta);
                newAlt.add(newNT);
            }
            newAlts.add(newAlt);
        }
        g.setProductions(nt, newAlts);

        // Build productions for the new non-terminal
        // APrime -> alpha1 APrime | alpha2 APrime | ... | @
        List<List<String>> primeAlts = new ArrayList<>();
        for (List<String> alpha : alphas) {
            List<String> primeAlt = new ArrayList<>(alpha);
            primeAlt.add(newNT);
            primeAlts.add(primeAlt);
        }
        // Add epsilon production
        List<String> epsilonAlt = new ArrayList<>();
        epsilonAlt.add("@");
        primeAlts.add(epsilonAlt);

        g.setProductions(newNT, primeAlts);
    }
}
