package com.example.satdsl.cadical;

import com.example.satdsl.api.SatResult;
import com.example.satdsl.api.SatSolver;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of SatSolver using CaDiCaL 2.2.0 via Java 25 FFM API.
 * 
 * This class binds to the CaDiCaL C library using the Foreign Function & Memory API.
 * The CaDiCaL library must be installed and available in the system library path.
 */
public class CaDiCaLSolver implements SatSolver {
    
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIBRARY_LOOKUP;
    
    private static final MethodHandle ccadical_init;
    private static final MethodHandle ccadical_release;
    private static final MethodHandle ccadical_add;
    private static final MethodHandle ccadical_solve;
    private static final MethodHandle ccadical_val;
    
    private final Arena arena;
    private final MemorySegment solverHandle;
    private int maxVariable = 0;
    private SatResult lastResult = null;
    
    static {
        try {
            // Try to load the CaDiCaL library
            // The library should be named libcadical.so (Linux), libcadical.dylib (macOS),
            // or cadical.dll (Windows)
            LIBRARY_LOOKUP = SymbolLookup.libraryLookup("cadical", Arena.ofAuto());
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load CaDiCaL library. Please ensure libcadical is installed " +
                "and available in your system library path (LD_LIBRARY_PATH on Linux, " +
                "DYLD_LIBRARY_PATH on macOS, or PATH on Windows).", e);
        }
        
        try {
            // Function signatures from CaDiCaL C API:
            // CCaDiCaL * ccadical_init (void);
            ccadical_init = LINKER.downcallHandle(
                LIBRARY_LOOKUP.find("ccadical_init").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS)
            );
            
            // void ccadical_release (CCaDiCaL *);
            ccadical_release = LINKER.downcallHandle(
                LIBRARY_LOOKUP.find("ccadical_release").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            );
            
            // void ccadical_add (CCaDiCaL *, int lit);
            ccadical_add = LINKER.downcallHandle(
                LIBRARY_LOOKUP.find("ccadical_add").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );
            
            // int ccadical_solve (CCaDiCaL *);
            ccadical_solve = LINKER.downcallHandle(
                LIBRARY_LOOKUP.find("ccadical_solve").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            
            // int ccadical_val (CCaDiCaL *, int lit);
            ccadical_val = LINKER.downcallHandle(
                LIBRARY_LOOKUP.find("ccadical_val").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );
        } catch (Throwable e) {
            throw new RuntimeException("Failed to bind CaDiCaL functions", e);
        }
    }
    
    /**
     * Creates a new CaDiCaL solver instance.
     */
    public CaDiCaLSolver() {
        this.arena = Arena.ofConfined();
        try {
            this.solverHandle = (MemorySegment) ccadical_init.invoke();
            if (solverHandle.address() == 0) {
                throw new RuntimeException("Failed to initialize CaDiCaL solver");
            }
        } catch (Throwable e) {
            arena.close();
            throw new RuntimeException("Failed to create CaDiCaL solver", e);
        }
    }
    
    @Override
    public void addClause(int[] literals) {
        if (literals == null || literals.length == 0) {
            throw new IllegalArgumentException("Clause cannot be null or empty");
        }
        
        try {
            for (int lit : literals) {
                if (lit != 0) {
                    ccadical_add.invoke(solverHandle, lit);
                    maxVariable = Math.max(maxVariable, Math.abs(lit));
                }
            }
            // Terminate the clause with 0
            ccadical_add.invoke(solverHandle, 0);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to add clause", e);
        }
    }
    
    @Override
    public SatResult solve() {
        try {
            int result = (int) ccadical_solve.invoke(solverHandle);
            
            // CaDiCaL returns:
            // 10 = SATISFIABLE
            // 20 = UNSATISFIABLE
            // 0 = UNKNOWN
            lastResult = switch (result) {
                case 10 -> SatResult.SATISFIABLE;
                case 20 -> SatResult.UNSATISFIABLE;
                default -> SatResult.UNKNOWN;
            };
            
            return lastResult;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to solve", e);
        }
    }
    
    @Override
    public int[] getModel() {
        if (lastResult != SatResult.SATISFIABLE) {
            return null;
        }
        
        try {
            int[] model = new int[maxVariable + 1];
            for (int var = 1; var <= maxVariable; var++) {
                int value = (int) ccadical_val.invoke(solverHandle, var);
                model[var] = value;
            }
            return model;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get model", e);
        }
    }
    
    @Override
    public void reset() {
        // CaDiCaL doesn't have a built-in reset, so we need to release and reinit
        close();
        try {
            MemorySegment newHandle = (MemorySegment) ccadical_init.invoke();
            if (newHandle.address() == 0) {
                throw new RuntimeException("Failed to reinitialize CaDiCaL solver");
            }
            // We can't reassign final fields, so this is a limitation
            // For a proper reset, users should create a new solver instance
            throw new UnsupportedOperationException(
                "Reset is not supported. Please create a new solver instance instead.");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to reset solver", e);
        }
    }
    
    @Override
    public void close() {
        try {
            if (solverHandle != null && solverHandle.address() != 0) {
                ccadical_release.invoke(solverHandle);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to release CaDiCaL solver", e);
        } finally {
            if (arena != null) {
                arena.close();
            }
        }
    }
}
