package com.constraints;

import org.springframework.roo.model.JavaType;

import java.util.ArrayList;

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
     * Annotate Simple Constraint with constraintType with list processing
     */
	String annotateConstraintSimple(ConstraintType constraintType, JavaType javaType, String fieldlist, String message, String applyIf);
	
	/**
     * Annotate Simple Constraint with constraintType for a single combination
     */
	String annotateConstraintSingle(ConstraintType constraintType, JavaType javaType, String field_subject, String field_object, String message, String applyIf, String newline, String message_return);
    
	/**
     * Provide and validate shell-string for fieldlist annotations.
     * Get valid Field List for class (size()>1) or returns an error Message, if input is invalid (size()==1)
     * 
     * @param string Input string with fieldnames.
     * @param javaType Class to annotate
     * @return If size()>1, valid list of fieldnames. If (size()==1, error Message.
     */
    ArrayList<String> getValidFieldList(String string, JavaType javaType);
    
    /**
     * Check whether field exists in class.
     * 
     * @param fieldname the name of the field.
     * @param javaType the class to check.
     * @return true, if field exists in class. false, if field doesn't exists in class.
     */
    boolean isFieldInClass(String fieldname, JavaType javaType);
    
    /**
     * Annotate raw expression constraint
     */
    String annotateConstraintRaw(JavaType javaType, String expression, String message, String applyIf, JavaType helpers, AnnotationState state);
    
    /**
     * Remove all annotations in class.
     * @param javaType the class.
     */
    void removeAllAnnoations(JavaType javaType);
}