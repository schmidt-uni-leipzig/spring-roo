package com.constraints;

import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a command type or an external add-on.
 *
 * @since 1.1
 */
public interface ConstraintsOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();
    
    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();
    
    /**
     * Annotate Raw Expression Constraint
     */
    void annotateConstraintRaw(JavaType paramClass, String expression, String message, String applyIf);
}