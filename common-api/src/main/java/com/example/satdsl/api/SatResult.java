package com.example.satdsl.api;

/**
 * Result of a SAT solver execution.
 */
public enum SatResult {
    /** The formula is satisfiable. */
    SATISFIABLE,
    
    /** The formula is unsatisfiable. */
    UNSATISFIABLE,
    
    /** The result is unknown (e.g., timeout or resource limit). */
    UNKNOWN
}
