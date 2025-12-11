# SAT DSL Demo

A multi-module Gradle project demonstrating integration of Xtext DSL with CaDiCaL SAT solver using Java's Foreign Function & Memory (FFM) API.

## Overview

This project showcases a complete architecture for integrating a Domain-Specific Language (DSL) built with Xtext with the CaDiCaL 2.2.0 SAT solver using Java's Foreign Function & Memory API. It demonstrates clean separation of concerns through a multi-module Gradle project and includes a mock solver for testing without external dependencies.

## Quick Start

```bash
# Clone the repository
git clone https://github.com/msun68/sat-dsl-demo.git
cd sat-dsl-demo

# Build the project
./gradlew build

# Run with mock solver (no CaDiCaL installation needed)
./gradlew :cli:run --args="--file samples/simple.satdsl --mock"
```

Expected output:
```
Problem: SimpleProblem
Variables: x, y, z
Constraints (3):
  x or y
  not x or z
  y or not z

Using mock solver (brute-force, for testing only)...
Solving...

Result:
-------
SATISFIABLE: Solution for SimpleProblem:
  x = false
  y = true
  z = false
```

## Architecture

The project consists of 4 modules that work together:

```
┌─────────────────────────────────────────────────────────┐
│  cli (Kotlin, Java 21)                                  │
│  - Entry point                                          │
│  - Calls DSL Generator                                  │
│  - Injects CaDiCaL Solver implementation                │
└─────────────────┬───────────────────────┬───────────────┘
                  │                       │
                  ▼                       ▼
┌─────────────────────────────┐ ┌─────────────────────────┐
│ dsl-generator (Java 21)     │ │ cadical-ffm (Java 21)   │
│ - Xtext-based DSL           │ │ - Foreign Function API  │
│ - Code generation           │ │ - Memory API            │
│ - Uses SatSolver interface  │ │ - CaDiCaL 2.2.0 bindings│
└──────────────┬──────────────┘ └─────────────┬───────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────┐
│  common-api (Java 21)                                   │
│  - SatSolver interface                                  │
│  - SatResult enum                                       │
│  - Shared data types                                    │
└─────────────────────────────────────────────────────────┘
```

### Key Architectural Pattern

The solution uses **dependency injection**: the CLI module creates a solver instance (CaDiCaL or mock) and injects it into the DSL generator via the common SatSolver interface. All modules compile to Java 21 bytecode and run on a Java 21+ JVM.

## Modules

### 1. common-api (Java 21)

Defines the interface contract for SAT solvers:
- `SatSolver` interface with methods for adding clauses, solving, and retrieving models
- `SatResult` enum: `SATISFIABLE`, `UNSATISFIABLE`, `UNKNOWN`
- `MockSolver`: A brute-force implementation for testing without CaDiCaL

### 2. cadical-ffm (Java 21 with FFM preview)

Implements the `SatSolver` interface using Java's Foreign Function & Memory API:
- Binds to CaDiCaL 2.2.0 C API functions
- Uses `Arena` for memory management
- Uses `Linker` and `SymbolLookup` for native function binding
- Provides error handling and resource management

### 3. dsl-generator (Java 21)

Provides DSL parsing and constraint solving:
- Simple parser for constraint satisfaction DSL
- Converts DSL constraints to CNF clauses
- Uses injected `SatSolver` to solve problems
- Generates human-readable output

### 4. cli (Kotlin, Java 21)

Command-line interface:
- Uses kotlinx-cli for argument parsing
- Reads and parses DSL files
- Wires together the DSL generator and solver (CaDiCaL or mock)
- Automatic fallback to mock solver if CaDiCaL is unavailable
- Handles file I/O and error reporting

## DSL Syntax

The DSL allows you to define constraint satisfaction problems:

```
problem ExampleProblem {
  variables: x, y, z
  
  constraints {
    x or y
    not x or z
    y or not z
  }
}
```

Features:
- Define named problems
- Declare boolean variables
- Express constraints using `or` and `not` operators
- Automatic conversion to CNF format

## Prerequisites

1. **Java 21 or later** - The project requires Java 21+ (with Java 22+ recommended for finalized FFM API)
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or use [SDKMAN](https://sdkman.io/)
   - Java 21 uses FFM as a preview feature (requires --enable-preview)
   - Java 22+ has FFM as a finalized feature

2. **CaDiCaL 2.2.0** - The SAT solver library (optional for testing with --mock flag)
   
   ### Installing CaDiCaL
   
   #### Linux/macOS:
   ```bash
   # Download and build CaDiCaL
   wget https://github.com/arminbiere/cadical/archive/refs/tags/rel-2.2.0.tar.gz
   tar xzf rel-2.2.0.tar.gz
   cd cadical-rel-2.2.0
   ./configure && make
   
   # Install the library (may require sudo)
   sudo make install
   
   # Or copy to a custom location and set library path
   mkdir -p ~/lib
   cp build/libcadical.so ~/lib/  # or libcadical.dylib on macOS
   export LD_LIBRARY_PATH=~/lib:$LD_LIBRARY_PATH  # Linux
   export DYLD_LIBRARY_PATH=~/lib:$DYLD_LIBRARY_PATH  # macOS
   ```
   
   #### Windows:
   - Building on Windows requires MinGW or Cygwin
   - Alternatively, use WSL (Windows Subsystem for Linux)

## Building

```bash
# Build all modules
./gradlew build

# Build a specific module
./gradlew :cli:build
```

## Running

### Using Gradle

```bash
# With CaDiCaL (if installed)
./gradlew :cli:run --args="--file samples/simple.satdsl"

# With mock solver (always works, no CaDiCaL needed)
./gradlew :cli:run --args="--file samples/simple.satdsl --mock"
```

### Using the JAR

```bash
# Build distribution
./gradlew :cli:installDist

# Run with CaDiCaL
./cli/build/install/cli/bin/cli --file samples/simple.satdsl

# Run with mock solver
./cli/build/install/cli/bin/cli --file samples/simple.satdsl --mock
```

### Command-line Options

```bash
sat-dsl-demo --file <path-to-dsl-file> [--mock]
sat-dsl-demo -f <path-to-dsl-file> [-m]

Options:
  -f, --file <path>    Path to the DSL file to parse and solve (required)
  -m, --mock           Use mock solver instead of CaDiCaL (optional)
```

**Note:** If CaDiCaL is not installed, the application will automatically fall back to the mock solver.

## Key Features

- ✅ **Multi-module Gradle project** with proper dependency management
- ✅ **Java 21 compatibility** across all modules with FFM preview features
- ✅ **Foreign Function & Memory API** bindings to native CaDiCaL library
- ✅ **DSL parser** for constraint satisfaction problems
- ✅ **Mock solver** for testing without external dependencies
- ✅ **Automatic fallback** when CaDiCaL is not available
- ✅ **Command-line interface** built with Kotlin and kotlinx-cli
- ✅ **Comprehensive documentation** and sample files

## Examples

### Example 1: Simple Satisfiable Problem

File: `samples/simple.satdsl`
```
problem SimpleProblem {
  variables: x, y, z
  
  constraints {
    x or y
    not x or z
    y or not z
  }
}
```

Output:
```
Problem: SimpleProblem
Variables: x, y, z
Constraints (3):
  x or y
  not x or z
  y or not z

Initializing CaDiCaL solver...
Solving...

Result:
-------
SATISFIABLE: Solution for SimpleProblem:
  x = true
  y = true
  z = true
```

### Example 2: Unsatisfiable Problem

File: `samples/unsatisfiable.satdsl`
```
problem UnsatisfiableProblem {
  variables: a, b
  
  constraints {
    a or b
    not a or b
    a or not b
    not a or not b
  }
}
```

Output:
```
...
Result:
-------
UNSATISFIABLE: No solution exists for UnsatisfiableProblem
```

## Testing

The project includes a mock SAT solver that can be used for testing without installing CaDiCaL:

```bash
# Test with mock solver
./gradlew :cli:run --args="--file samples/simple.satdsl --mock"
```

The mock solver uses a brute-force approach and is limited to problems with ≤20 variables. It's suitable for:
- Testing the DSL parser and generator
- Verifying the architecture
- Learning how the system works
- Small constraint problems

For production use or larger problems, install and use CaDiCaL.

## Development

### Project Structure

```
sat-dsl-demo/
├── common-api/           # Shared interfaces (Java 21)
│   └── src/main/java/
├── cadical-ffm/          # CaDiCaL bindings (Java 21)
│   └── src/main/java/
├── dsl-generator/        # DSL parser and generator (Java 21)
│   └── src/main/java/
├── cli/                  # CLI application (Kotlin, Java 21)
│   └── src/main/kotlin/
├── samples/              # Sample DSL files
├── build.gradle.kts      # Root build configuration
├── settings.gradle.kts   # Multi-module settings
└── README.md
```

### Adding New Constraints

The DSL currently supports basic boolean constraints with `or` and `not` operators. To add more complex constraint types:

1. Update the parser in `SatDslParser.java`
2. Modify the constraint parsing in `SatDslGenerator.java`
3. Add test cases in the `samples/` directory

## Technical Details

### Java Version Strategy

- **Java 21**: All modules compile to Java 21 bytecode for compatibility
- **FFM API**: Available as preview in Java 21, finalized in Java 22+
- **Preview Features**: cadical-ffm and cli modules use --enable-preview flag
- **Runtime**: Java 21+ JVM can execute all modules

### FFM API Usage

The `CaDiCaLSolver` class demonstrates:
- `SymbolLookup.libraryLookup()` - Loading native libraries
- `Linker.downcallHandle()` - Creating method handles for C functions
- `Arena.ofConfined()` - Managing native memory
- `MemorySegment` - Working with native pointers

### Gradle Configuration

- Multi-module project with shared repositories
- Per-module Java toolchain configuration
- Kotlin plugin applied only to CLI module
- Preview features enabled for FFM API in Java 21

## Troubleshooting

### CaDiCaL library not found

Error: `Failed to load CaDiCaL library`

Solutions:
1. Ensure CaDiCaL is installed and library is accessible
2. Set the library path environment variable:
   - Linux: `export LD_LIBRARY_PATH=/path/to/cadical/lib:$LD_LIBRARY_PATH`
   - macOS: `export DYLD_LIBRARY_PATH=/path/to/cadical/lib:$DYLD_LIBRARY_PATH`
   - Windows: Add the library directory to `PATH`

### Java version mismatch

Ensure you're using Java 21 or later:
```bash
java -version  # Should show version 21 or later
./gradlew --version  # Check Gradle is using Java 21+
```

### Build failures

```bash
# Clean and rebuild
./gradlew clean build

# Build with stacktrace for debugging
./gradlew build --stacktrace
```

## License

This project is provided as a demonstration example.

## References

- [Xtext Documentation](https://www.eclipse.org/Xtext/documentation/index.html)
- [CaDiCaL SAT Solver](https://github.com/arminbiere/cadical)
- [Java FFM API](https://openjdk.org/jeps/454)
- [Kotlin CLI](https://github.com/Kotlin/kotlinx-cli)
