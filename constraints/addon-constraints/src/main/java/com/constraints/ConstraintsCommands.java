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
    @CliAvailabilityIndicator({ "constraints setup", "constraints add", "constraints all" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }
    
    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     * 
     * @param type 
     * @param attribute 
     * @param scope 
     */
    @CliCommand(value = "constraints add", help = "Defines a certain constrait szenario")
    public void add(
		@CliOption(
			key = "type", 
			mandatory = true, 
			help = "Java method name thats suppose to validate the annotation with"
		) String paramType,
		@CliOption(
			key = "class", 
			mandatory = true, 
			help = "The class (e.g. from java type entity) to apply this constraint annotation to."
		) JavaType paramClass,
		@CliOption(
			key = "field1", 
			mandatory = true, 
			help = "Fieldname (String)"
		) String paramField1,
		@CliOption(
			key = "field2", 
			mandatory = true, 
			help = "Fieldname (String)"
		) String paramField2
    ){
    		operations.annotateConstraint(paramType, paramClass, paramField1, paramField2);
    }
    
    /**
     * This method registers a command with the Roo shell. It has no command attribute.
     * 
     */
    @CliCommand(value = "constraints all", help = "Shows all methods in Constraints addon")
    public void all() {
        operations.annotateAll();
    }
    
    /**
     * This method registers a command with the Roo shell. It has no command attribute.
     * 
     */
    @CliCommand(value = "constraints setup", help = "Setup Constraints addon")
    public void setup() {
        operations.setup();
    }
}