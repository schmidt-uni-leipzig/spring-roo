package com.constraints;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related logic in this
 * class. You can return any objects from each method, or use the logger directly if you'd
 * like to emit messages of different severity (and therefore different colours on 
 * non-Windows systems).
 * 
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class ConstraintsCommands implements CommandMarker { // All command types must implement the CommandMarker interface
    
    /**
     * Get a reference to the ConstraintsOperations from the underlying OSGi container
     */
    @Reference private ConstraintsOperations operations;
    
    /**
     * This method is optional. It allows automatic command hiding in situations when the command should not be visible.
     * For example the 'entity' command will not be made available before the user has defined his persistence settings 
     * in the Roo shell or directly in the project.
     * 
     * You can define multiple methods annotated with {@link CliAvailabilityIndicator} if your commands have differing
     * visibility requirements.
     * 
     * @return true (default) if the command should be visible at this stage, false otherwise
     */
    @CliAvailabilityIndicator({ "constraints setup", "constraints equals", "constraints notEquals", "constraints intersected", "constraints notIntersected", "constraints rawExpression" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }
    
    
    // ----- BEGIN ----- SIMPLE CONSTRAINT DEFINITIONS ----- BEGIN -----
    
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param class 
     * @param field1 
     * @param field2
     * @param message 
     */
    @CliCommand(value = "constraints equals", help = "Defines an equals constraint.")
    public void equals(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to."
		) JavaType paramClass,
		@CliOption(	key = "field1",	mandatory = true, help = "Fieldname (String)"
		) String field1,
		@CliOption(	key = "field2", mandatory = true, help = "Fieldname (String)"
		) String field2,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message
    ){
		//convert input into expression
		String expression = field1 + ".equals("+field2+")";
		
		operations.annotateConstraintRaw(paramClass, expression, message, null);
    }
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param class 
     * @param field1 
     * @param field2
     * @param message 
     */
    @CliCommand(value = "constraints notEquals", help = "Defines a not equals constraint.")
    public void notEquals(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to."
		) JavaType paramClass,
		@CliOption(	key = "field1",	mandatory = true, help = "Fieldname (String)"
		) String field1,
		@CliOption(	key = "field2", mandatory = true, help = "Fieldname (String)"
		) String field2,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message
    ){
    	//convert input into expression
		String expression = "!" + field1 + ".equals("+field2+")";
		
		operations.annotateConstraintRaw(paramClass, expression, message, null);
    }
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param class 
     * @param field1 
     * @param field2
     * @param message 
     */
    @CliCommand(value = "constraints intersected", help = "Defines an intersected constraint.")
    public void intersected(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to."
		) JavaType paramClass,
		@CliOption(	key = "field1",	mandatory = true, help = "Fieldname (String)"
		) String field1,
		@CliOption(	key = "field2", mandatory = true, help = "Fieldname (String)"
		) String field2,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message
    ){
    	//convert input into expression
		String expression = "TODO intersectedExpression"; 	//TODO Intersected Expression!!!!!!
		
		operations.annotateConstraintRaw(paramClass, expression, message, null);
    }
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param class 
     * @param field1 
     * @param field2
     * @param message 
     */
    @CliCommand(value = "constraints notIntersected", help = "Defines a not intersected constraint.")
    public void notIntersected(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to."
		) JavaType paramClass,
		@CliOption(	key = "field1",	mandatory = true, help = "Fieldname (String)"
		) String field1,
		@CliOption(	key = "field2", mandatory = true, help = "Fieldname (String)"
		) String field2,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message
    ){
    	//convert input into expression
		String expression = "TODO notIntersectedExpression"; 	//TODO NotIntersected Expression!!!!!!
		
		operations.annotateConstraintRaw(paramClass, expression, message, null);
    }
    
    
    // ----- END ----- SIMPLE CONSTRAINT DEFINITIONS ----- END -----
    
    // ----- BEGIN ----- RAW CONSTRAINT DEFINITIONS ----- BEGIN -----
    
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param expression 
     * @param class 
     * @param message 
     */
    @CliCommand(value = "constraints rawExpression", help = "Defines a custom constraint szenario")
    public void rawExpression(
		@CliOption(
			key = "class", 
			mandatory = true, 
			help = "The class (e.g. from java type entity) to apply this constraint annotation to."
		) JavaType paramClass,
		@CliOption(
			key = "expression", 
			mandatory = true, 
			help = "Raw expression to validate the annotation with"
		) String rawExpression,
		@CliOption(
			key = "message", 
			mandatory = true, 
			help = "Message text (String)"
		) String message,
		@CliOption(
			key = "applyIf", 
			mandatory = false, 
			help = "TODO" 			//TODO
		) String applyIf
    ){
    		operations.annotateConstraintRaw(paramClass, rawExpression, message, applyIf);
    }
    
    
    
    // ----- END ----- RAW CONSTRAINT DEFINITIONS ----- END -----
    
    
    
    /**
     * This method registers a command with the Roo shell. It has no command attribute.
     * 
     */
    @CliCommand(value = "constraints setup", help = "Setup Constraints addon")
    public void setup() {
        operations.setup();
    }
}