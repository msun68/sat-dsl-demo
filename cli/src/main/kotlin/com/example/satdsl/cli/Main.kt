package com.example.satdsl.cli

import com.example.satdsl.api.SatSolver
import com.example.satdsl.api.mock.MockSolver
import com.example.satdsl.cadical.CaDiCaLSolver
import com.example.satdsl.dsl.SatDslGenerator
import com.example.satdsl.dsl.SatDslParser
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val parser = ArgParser("sat-dsl-demo")
    
    val inputFile by parser.option(
        ArgType.String,
        shortName = "f",
        fullName = "file",
        description = "Path to the DSL file to parse and solve"
    ).required()
    
    val useMock by parser.option(
        ArgType.Boolean,
        shortName = "m",
        fullName = "mock",
        description = "Use mock solver instead of CaDiCaL"
    ).default(false)
    
    try {
        parser.parse(args)
    } catch (e: Exception) {
        println("Error: ${e.message}")
        println()
        println("Usage: sat-dsl-demo --file <path-to-dsl-file> [--mock]")
        println("       sat-dsl-demo -f <path-to-dsl-file> [-m]")
        exitProcess(1)
    }
    
    // Check if file exists
    val file = File(inputFile)
    if (!file.exists()) {
        println("Error: File not found: $inputFile")
        exitProcess(1)
    }
    
    try {
        // Read the DSL file
        val dslText = file.readText()
        
        println("Parsing DSL file: $inputFile")
        println()
        
        // Parse the DSL
        val problem = SatDslParser.parse(dslText)
        
        println("Problem: ${problem.name}")
        println("Variables: ${problem.variables.joinToString(", ")}")
        println("Constraints (${problem.constraints.size}):")
        problem.constraints.forEach { constraint ->
            println("  $constraint")
        }
        println()
        
        // Create the solver
        val solver: SatSolver = if (useMock) {
            println("Using mock solver (brute-force, for testing only)...")
            MockSolver()
        } else {
            try {
                println("Initializing CaDiCaL solver...")
                CaDiCaLSolver()
            } catch (e: Exception) {
                println("Warning: Could not load CaDiCaL library: ${e.message}")
                println("Falling back to mock solver...")
                MockSolver()
            }
        }
        
        solver.use {
            // Create the generator and solve
            println("Solving...")
            val generator = SatDslGenerator(solver)
            val solution = generator.solve(problem)
            
            println()
            println("Result:")
            println("-------")
            println(SatDslGenerator.formatSolution(problem, solution))
        }
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}
