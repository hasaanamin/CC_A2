/*
 * ParserStack.java
 * CS4031 - Compiler Construction, Assignment 02
 * LL(1) Parser Design & Implementation - Spring 2026
 *
 * Group Members:
 *   - 23i-0665 Muhammad Tabarak Cheema
 *   - 23i-0019 Hasaan Amin
 *
 * Description:
 *   Stack implementation for the LL(1) predictive parser.
 *   Uses an ArrayList internally. Provides push, pop, top,
 *   isEmpty, and display operations.
 */

import java.util.*;

public class ParserStack {

    // Internal storage - index 0 is bottom of stack ($), last index is top
    private List<String> stack;

    /**
     * Constructs a new parser stack initialized with $ and the start symbol.
     *
     * @param startSymbol the start symbol of the grammar
     */
    public ParserStack(String startSymbol) {
        stack = new ArrayList<>();
        stack.add("$");            // Bottom marker
        stack.add(startSymbol);    // Start symbol on top
    }

    /**
     * Pushes a symbol onto the top of the stack.
     *
     * @param symbol the symbol to push
     */
    public void push(String symbol) {
        stack.add(symbol);
    }

    /**
     * Pops and returns the top symbol from the stack.
     *
     * @return the symbol that was on top
     * @throws EmptyStackException if the stack is empty
     */
    public String pop() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        return stack.remove(stack.size() - 1);
    }

    /**
     * Returns the top symbol without removing it.
     *
     * @return the symbol currently on top
     * @throws EmptyStackException if the stack is empty
     */
    public String top() {
        if (stack.isEmpty()) {
            throw new EmptyStackException();
        }
        return stack.get(stack.size() - 1);
    }

    /**
     * Checks if the stack is empty.
     *
     * @return true if the stack has no elements
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Returns the current size of the stack.
     *
     * @return number of elements in the stack
     */
    public int size() {
        return stack.size();
    }

    /**
     * Returns a string representation of the stack contents (bottom to top).
     * Example: "$ Expr ExprPrime"
     *
     * @return formatted string of stack contents
     */
    public String getContents() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(stack.get(i));
        }
        return sb.toString();
    }

    /**
     * Returns a copy of the stack as a list (bottom to top).
     *
     * @return list copy of the stack
     */
    public List<String> toList() {
        return new ArrayList<>(stack);
    }

    @Override
    public String toString() {
        return getContents();
    }
}
