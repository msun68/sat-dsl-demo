package com.example.satdsl.dsl;

import java.util.List;

/**
 * Represents a parsed SAT problem from the DSL.
 */
public record SatProblem(
    String name,
    List<String> variables,
    List<String> constraints
) {}
