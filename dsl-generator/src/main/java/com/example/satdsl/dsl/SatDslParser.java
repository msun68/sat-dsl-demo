package com.example.satdsl.dsl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple parser for the SAT DSL.
 * 
 * Example DSL syntax:
 * <pre>
 * problem ExampleProblem {
 *   variables: x, y, z
 *   
 *   constraints {
 *     x or y
 *     not x or z
 *     y or not z
 *   }
 * }
 * </pre>
 */
public class SatDslParser {
    
    private static final Pattern PROBLEM_PATTERN = Pattern.compile("problem\\s+(\\w+)\\s*\\{");
    private static final Pattern VARIABLES_PATTERN = Pattern.compile("variables:\\s*(.+)");
    private static final Pattern CONSTRAINTS_PATTERN = Pattern.compile("constraints\\s*\\{");
    
    /**
     * Parses a DSL string into a SatProblem.
     */
    public static SatProblem parse(String dslText) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(dslText));
        
        String problemName = null;
        List<String> variableNames = new ArrayList<>();
        List<String> constraints = new ArrayList<>();
        
        String line;
        boolean inConstraints = false;
        int braceDepth = 0;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            
            // Match problem name
            Matcher problemMatcher = PROBLEM_PATTERN.matcher(line);
            if (problemMatcher.find()) {
                problemName = problemMatcher.group(1);
                braceDepth++;
                continue;
            }
            
            // Match variables
            Matcher varMatcher = VARIABLES_PATTERN.matcher(line);
            if (varMatcher.find()) {
                String varList = varMatcher.group(1);
                String[] vars = varList.split(",");
                for (String var : vars) {
                    variableNames.add(var.trim());
                }
                continue;
            }
            
            // Match constraints section
            Matcher constraintsMatcher = CONSTRAINTS_PATTERN.matcher(line);
            if (constraintsMatcher.find()) {
                inConstraints = true;
                braceDepth++;
                continue;
            }
            
            // Handle closing braces
            if (line.contains("}")) {
                braceDepth--;
                if (braceDepth == 1) {
                    inConstraints = false;
                }
                continue;
            }
            
            // Collect constraint lines
            if (inConstraints && !line.isEmpty()) {
                constraints.add(line);
            }
        }
        
        if (problemName == null) {
            throw new IllegalArgumentException("No problem name found");
        }
        
        if (variableNames.isEmpty()) {
            throw new IllegalArgumentException("No variables declared");
        }
        
        return new SatProblem(problemName, variableNames, constraints);
    }
}
