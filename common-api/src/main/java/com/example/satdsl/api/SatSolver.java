package com.example.satdsl.api;

/**
 * Interface for SAT solvers.
 * 
 * A SAT solver determines whether a boolean formula in Conjunctive Normal Form (CNF)
 * is satisfiable. Variables are represented as positive integers, and their negations
 * are represented as negative integers.
 */
public interface SatSolver extends AutoCloseable {
    
    /**
     * Adds a clause to the solver.
     * A clause is a disjunction (OR) of literals.
     * The clause is terminated by a 0 in the array, or by the end of the array.
     * 
     * @param literals Array of literals (non-zero integers). Positive integers represent
     *                 variables, negative integers represent negated variables.
     *                 For example, [1, -2, 3] represents the clause (x1 OR NOT x2 OR x3).
     */
    void addClause(int[] literals);
    
    /**
     * Solves the current formula.
     * 
     * @return The result of the solving attempt.
     */
    SatResult solve();
    
    /**
     * Returns the model (variable assignment) if the formula is satisfiable.
     * Must be called after solve() returns SATISFIABLE.
     * 
     * @return Array where the index represents the variable number (1-indexed),
     *         and the value represents the assignment (true/false).
     *         Returns null if the formula is not satisfiable or solve() hasn't been called.
     */
    int[] getModel();
    
    /**
     * Resets the solver to its initial state, removing all clauses.
     */
    void reset();
    
    /**
     * Closes the solver and releases all resources.
     */
    @Override
    void close();
}
