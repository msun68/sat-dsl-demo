package com.example.satdsl.cli

import com.example.satdsl.api.SatSolver
import com.example.satdsl.cadical.CaDiCaLSolver
import com.example.satdsl.dsl.SatDslGenerator
import com.example.satdsl.dsl.SatDslParser
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
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
    
    try {
        parser.parse(args)
    } catch (e: Exception) {
        println("Error: ${e.message}")
        println()
        println("Usage: sat-dsl-demo --file <path-to-dsl-file>")
        println("       sat-dsl-demo -f <path-to-dsl-file>")
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
        
        // Create the CaDiCaL solver (Java 25 FFM)
        println("Initializing CaDiCaL solver...")
        val solver: SatSolver = CaDiCaLSolver()
        
        solver.use {
            // Create the generator and solve (Java 21 DSL generator using injected solver)
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
