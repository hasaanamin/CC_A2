/*
 * Tree.java
 * CS4031 - Compiler Construction, Assignment 02
 * LL(1) Parser Design & Implementation - Spring 2026
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Parse tree data structure and generation for LL(1) parser.
 *   Each node stores a symbol (terminal or non-terminal) and its children.
 *   Provides ASCII-art display, indented text format, and preorder traversal.
 */

import java.io.*;
import java.util.*;

public class Tree {

    // ===== Inner class: TreeNode =====

    /**
     * Represents a single node in the parse tree.
     */
    public static class TreeNode {
        String symbol;              // The grammar symbol at this node
        List<TreeNode> children;    // Children list (empty for leaf/terminal nodes)
        boolean isTerminal;         // Whether this is a terminal/leaf node

        /**
         * Creates a new tree node for the given symbol.
         *
         * @param symbol     the grammar symbol
         * @param isTerminal true if this is a terminal (leaf) node
         */
        public TreeNode(String symbol, boolean isTerminal) {
            this.symbol = symbol;
            this.isTerminal = isTerminal;
            this.children = new ArrayList<>();
        }

        /**
         * Adds a child node.
         *
         * @param child the child TreeNode to add
         */
        public void addChild(TreeNode child) {
            children.add(child);
        }
    }

    // ===== Tree fields =====

    private TreeNode root;  // Root of the parse tree

    /**
     * Creates an empty parse tree.
     */
    public Tree() {
        this.root = null;
    }

    /**
     * Creates a parse tree with the given root node.
     *
     * @param root the root TreeNode
     */
    public Tree(TreeNode root) {
        this.root = root;
    }

    /**
     * Returns the root node of the tree.
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Sets the root node of the tree.
     */
    public void setRoot(TreeNode root) {
        this.root = root;
    }

    // ===== Display methods =====

    /**
     * Returns the parse tree as an indented text string.
     * Internal nodes (non-terminals) show their children indented below.
     * Leaf nodes (terminals) are marked with quotes.
     *
     * @return the indented text representation
     */
    public String toIndentedText() {
        if (root == null) return "(empty tree)";
        StringBuilder sb = new StringBuilder();
        buildIndentedText(root, sb, "", true);
        return sb.toString();
    }

    /**
     * Recursive helper for indented text generation.
     */
    private void buildIndentedText(TreeNode node, StringBuilder sb,
                                    String prefix, boolean isLast) {
        sb.append(prefix);
        sb.append(isLast ? "└── " : "├── ");

        if (node.isTerminal) {
            if (node.symbol.equals("@")) {
                sb.append("ε");
            } else {
                sb.append("\"").append(node.symbol).append("\"");
            }
        } else {
            sb.append(node.symbol);
        }
        sb.append("\n");

        String childPrefix = prefix + (isLast ? "    " : "│   ");

        for (int i = 0; i < node.children.size(); i++) {
            boolean last = (i == node.children.size() - 1);
            buildIndentedText(node.children.get(i), sb, childPrefix, last);
        }
    }

    /**
     * Returns the parse tree as ASCII art (horizontal tree).
     *
     * @return ASCII art string
     */
    public String toASCIIArt() {
        if (root == null) return "(empty tree)";
        StringBuilder sb = new StringBuilder();
        sb.append("\n  Parse Tree (ASCII):\n");
        sb.append("  " + "─".repeat(40) + "\n");
        buildASCII(root, sb, "  ", true);
        sb.append("  " + "─".repeat(40) + "\n");
        return sb.toString();
    }

    /**
     * Recursive helper for ASCII art generation.
     */
    private void buildASCII(TreeNode node, StringBuilder sb,
                             String prefix, boolean isLast) {
        sb.append(prefix);
        sb.append(isLast ? "└─ " : "├─ ");

        if (node.isTerminal) {
            if (node.symbol.equals("@")) {
                sb.append("[ε]");
            } else {
                sb.append("[").append(node.symbol).append("]");
            }
        } else {
            sb.append(node.symbol);
        }
        sb.append("\n");

        String childPrefix = prefix + (isLast ? "   " : "│  ");

        for (int i = 0; i < node.children.size(); i++) {
            boolean last = (i == node.children.size() - 1);
            buildASCII(node.children.get(i), sb, childPrefix, last);
        }
    }

    /**
     * Returns the preorder traversal of the parse tree.
     *
     * @return list of symbols in preorder
     */
    public List<String> preorderTraversal() {
        List<String> result = new ArrayList<>();
        if (root != null) {
            preorder(root, result);
        }
        return result;
    }

    /**
     * Recursive helper for preorder traversal.
     */
    private void preorder(TreeNode node, List<String> result) {
        result.add(node.symbol);
        for (TreeNode child : node.children) {
            preorder(child, result);
        }
    }

    /**
     * Returns the postorder traversal of the parse tree.
     *
     * @return list of symbols in postorder
     */
    public List<String> postorderTraversal() {
        List<String> result = new ArrayList<>();
        if (root != null) {
            postorder(root, result);
        }
        return result;
    }

    /**
     * Recursive helper for postorder traversal.
     */
    private void postorder(TreeNode node, List<String> result) {
        for (TreeNode child : node.children) {
            postorder(child, result);
        }
        result.add(node.symbol);
    }

    /**
     * Displays the parse tree on the console.
     *
     * @param title a title to display above the tree
     */
    public void display(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
        System.out.println(toASCIIArt());
        System.out.println("  Preorder:  " + String.join(" ", preorderTraversal()));
        System.out.println("  Postorder: " + String.join(" ", postorderTraversal()));
        System.out.println("=".repeat(60));
    }

    /**
     * Writes the parse tree to an output file.
     *
     * @param filePath the output file path
     * @param title    a title for the section
     */
    public void writeToFile(String filePath, String title) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filePath, true));
        writer.println("\n" + "=".repeat(60));
        writer.println("  " + title);
        writer.println("=".repeat(60));
        writer.println(toASCIIArt());
        writer.println("  Indented Text Format:");
        writer.println(toIndentedText());
        writer.println("  Preorder:  " + String.join(" ", preorderTraversal()));
        writer.println("  Postorder: " + String.join(" ", postorderTraversal()));
        writer.println("=".repeat(60));
        writer.close();
    }
}
