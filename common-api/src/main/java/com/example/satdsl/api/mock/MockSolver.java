package com.example.satdsl.api.mock;

import com.example.satdsl.api.SatResult;
import com.example.satdsl.api.SatSolver;

import java.util.*;

/**
 * A simple mock SAT solver for testing purposes.
 * This solver uses a naive brute-force approach and is not suitable for production use.
 * It is provided as a fallback when CaDiCaL library is not available.
 */
public class MockSolver implements SatSolver {
    
    private final List<int[]> clauses = new ArrayList<>();
    private int maxVariable = 0;
    private int[] lastModel = null;
    
    @Override
    public void addClause(int[] literals) {
        if (literals == null || literals.length == 0) {
            throw new IllegalArgumentException("Clause cannot be null or empty");
        }
        
        // Store a copy of the clause
        int[] clause = new int[literals.length];
        for (int i = 0; i < literals.length; i++) {
            clause[i] = literals[i];
            if (literals[i] != 0) {
                maxVariable = Math.max(maxVariable, Math.abs(literals[i]));
            }
        }
        clauses.add(clause);
    }
    
    @Override
    public SatResult solve() {
        if (clauses.isEmpty()) {
            lastModel = new int[1];
            return SatResult.SATISFIABLE;
        }
        
        // Try all possible assignments (brute force)
        // This is exponential and only works for small problems!
        int numVars = maxVariable;
        long numAssignments = 1L << numVars;
        
        // Limit to prevent timeout on large problems
        if (numVars > 20) {
            return SatResult.UNKNOWN;
        }
        
        for (long assignment = 0; assignment < numAssignments; assignment++) {
            if (isSatisfied(assignment, numVars)) {
                lastModel = extractModel(assignment, numVars);
                return SatResult.SATISFIABLE;
            }
        }
        
        lastModel = null;
        return SatResult.UNSATISFIABLE;
    }
    
    private boolean isSatisfied(long assignment, int numVars) {
        for (int[] clause : clauses) {
            boolean clauseSatisfied = false;
            for (int literal : clause) {
                if (literal == 0) continue;
                
                int var = Math.abs(literal);
                boolean value = ((assignment >> (var - 1)) & 1) == 1;
                
                if (literal > 0) {
                    // Positive literal: satisfied if variable is true
                    if (value) {
                        clauseSatisfied = true;
                        break;
                    }
                } else {
                    // Negative literal: satisfied if variable is false
                    if (!value) {
                        clauseSatisfied = true;
                        break;
                    }
                }
            }
            
            if (!clauseSatisfied) {
                return false;
            }
        }
        
        return true;
    }
    
    private int[] extractModel(long assignment, int numVars) {
        int[] model = new int[numVars + 1];
        for (int var = 1; var <= numVars; var++) {
            boolean value = ((assignment >> (var - 1)) & 1) == 1;
            model[var] = value ? var : -var;
        }
        return model;
    }
    
    @Override
    public int[] getModel() {
        return lastModel;
    }
    
    @Override
    public void reset() {
        clauses.clear();
        maxVariable = 0;
        lastModel = null;
    }
    
    @Override
    public void close() {
        reset();
    }
}
