/*
 * Main.java
 * CS4031 - Compiler Construction, Assignment 02
 * LL(1) Parser Design & Implementation - Spring 2026
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Main entry point for the LL(1) Parser.
 *   Part 1 Pipeline: Read CFG -> Left Factor -> Remove Left Recursion
 *                     -> Compute FIRST -> Compute FOLLOW -> Build LL(1) Table
 *
 * Usage:
 *   javac *.java
 *   java Main <grammar_file> [output_file]
 *
 *   Examples:
 *     java Main ../input/grammar1.txt
 *     java Main ../input/grammar1.txt ../output/grammar1_output.txt
 */

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        // Default paths
        String grammarFile = "../input/grammar1.txt";
        String outputFile = "../output/output.txt";

        // Parse command-line arguments
        if (args.length >= 1) {
            grammarFile = args[0];
        }
        if (args.length >= 2) {
            outputFile = args[1];
        }

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       LL(1) Parser - Part 1: Grammar Analysis           ║");
        System.out.println("║       CS4031 - Compiler Construction (Spring 2026)       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            // Clear the output file if it exists
            PrintWriter clearWriter = new PrintWriter(new FileWriter(outputFile, false));
            clearWriter.println("LL(1) Parser - Part 1: Grammar Analysis Output");
            clearWriter.println("Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            clearWriter.close();

            // ============================================================
            // STEP 1: Read the CFG from file
            // ============================================================
            System.out.println("\n>>> Step 1: Reading grammar from: " + grammarFile);
            Grammar originalGrammar = Grammar.loadFromFile(grammarFile);
            originalGrammar.display("Original Grammar");
            originalGrammar.writeToFile(outputFile, "Original Grammar");

            // ============================================================
            // STEP 2: Apply Left Factoring
            // ============================================================
            System.out.println("\n>>> Step 2: Applying Left Factoring...");
            Grammar factoredGrammar = LeftFactoring.apply(originalGrammar);
            factoredGrammar.display("Grammar After Left Factoring");
            factoredGrammar.writeToFile(outputFile, "Grammar After Left Factoring");

            // ============================================================
            // STEP 3: Remove Left Recursion (Direct + Indirect)
            // ============================================================
            System.out.println("\n>>> Step 3: Removing Left Recursion...");
            Grammar transformedGrammar = LeftRecursion.apply(factoredGrammar);
            transformedGrammar.display("Grammar After Left Recursion Removal");
            transformedGrammar.writeToFile(outputFile, "Grammar After Left Recursion Removal");

            // ============================================================
            // STEP 4: Compute FIRST Sets
            // ============================================================
            System.out.println("\n>>> Step 4: Computing FIRST Sets...");
            Map<String, Set<String>> firstSets = FirstFollow.computeFirst(transformedGrammar);
            FirstFollow.displaySets("FIRST Sets", firstSets);
            FirstFollow.writeSetsToFile(outputFile, "FIRST Sets", firstSets);

            // ============================================================
            // STEP 5: Compute FOLLOW Sets
            // ============================================================
            System.out.println("\n>>> Step 5: Computing FOLLOW Sets...");
            Map<String, Set<String>> followSets = FirstFollow.computeFollow(transformedGrammar, firstSets);
            FirstFollow.displaySets("FOLLOW Sets", followSets);
            FirstFollow.writeSetsToFile(outputFile, "FOLLOW Sets", followSets);

            // ============================================================
            // STEP 6: Construct LL(1) Parsing Table
            // ============================================================
            System.out.println("\n>>> Step 6: Constructing LL(1) Parsing Table...");
            ParsingTable parsingTable = ParsingTable.construct(transformedGrammar, firstSets, followSets);
            parsingTable.display();
            parsingTable.writeToFile(outputFile);

            // ============================================================
            // Summary
            // ============================================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("  SUMMARY");
            System.out.println("=".repeat(60));
            System.out.println("  Grammar file:     " + grammarFile);
            System.out.println("  Non-terminals:    " + transformedGrammar.getNonTerminals().size());
            System.out.println("  Terminals:        " + transformedGrammar.getTerminals().size());
            System.out.println("  LL(1) status:     " + (parsingTable.isLL1() ? "YES" : "NO (conflicts exist)"));
            System.out.println("  Output written to: " + outputFile);
            System.out.println("=".repeat(60));

        } catch (FileNotFoundException e) {
            System.err.println("\nError: Grammar file not found: " + grammarFile);
            System.err.println("Please check the file path and try again.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("\nError: I/O error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\nUnexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
