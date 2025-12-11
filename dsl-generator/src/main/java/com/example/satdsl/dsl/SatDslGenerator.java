package com.example.satdsl.dsl;

import com.example.satdsl.api.SatResult;
import com.example.satdsl.api.SatSolver;

import java.util.*;

/**
 * Generates CNF clauses from DSL constraints and solves them using a SAT solver.
 */
public class SatDslGenerator {
    
    private final SatSolver solver;
    
    public SatDslGenerator(SatSolver solver) {
        this.solver = solver;
    }
    
    /**
     * Solves a SAT problem defined in the DSL.
     * 
     * @param problem The parsed problem
     * @return A map of variable names to their boolean values, or null if unsatisfiable
     */
    public Map<String, Boolean> solve(SatProblem problem) {
        // Create a mapping from variable names to integers (1-indexed)
        Map<String, Integer> varMap = new HashMap<>();
        for (int i = 0; i < problem.variables().size(); i++) {
            varMap.put(problem.variables().get(i), i + 1);
        }
        
        // Parse and add each constraint
        for (String constraint : problem.constraints()) {
            int[] clause = parseConstraint(constraint, varMap);
            solver.addClause(clause);
        }
        
        // Solve
        SatResult result = solver.solve();
        
        if (result != SatResult.SATISFIABLE) {
            return null;
        }
        
        // Extract the model
        int[] model = solver.getModel();
        Map<String, Boolean> solution = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : varMap.entrySet()) {
            String varName = entry.getKey();
            int varIndex = entry.getValue();
            
            if (varIndex < model.length) {
                // CaDiCaL returns positive value if true, negative if false
                solution.put(varName, model[varIndex] > 0);
            }
        }
        
        return solution;
    }
    
    /**
     * Parses a constraint string into a CNF clause.
     * 
     * Example: "x or not y or z" -> [1, -2, 3]
     */
    private int[] parseConstraint(String constraint, Map<String, Integer> varMap) {
        // Remove extra whitespace
        constraint = constraint.trim();
        
        // Split by "or"
        String[] literals = constraint.split("\\s+or\\s+");
        
        List<Integer> clause = new ArrayList<>();
        
        for (String literal : literals) {
            literal = literal.trim();
            
            boolean negated = false;
            if (literal.startsWith("not ")) {
                negated = true;
                literal = literal.substring(4).trim();
            }
            
            Integer varNum = varMap.get(literal);
            if (varNum == null) {
                throw new IllegalArgumentException("Unknown variable: " + literal);
            }
            
            clause.add(negated ? -varNum : varNum);
        }
        
        return clause.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * Formats the solution for display.
     */
    public static String formatSolution(SatProblem problem, Map<String, Boolean> solution) {
        if (solution == null) {
            return "UNSATISFIABLE: No solution exists for " + problem.name();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("SATISFIABLE: Solution for ").append(problem.name()).append(":\n");
        
        for (String var : problem.variables()) {
            Boolean value = solution.get(var);
            sb.append("  ").append(var).append(" = ").append(value).append("\n");
        }
        
        return sb.toString();
    }
}
