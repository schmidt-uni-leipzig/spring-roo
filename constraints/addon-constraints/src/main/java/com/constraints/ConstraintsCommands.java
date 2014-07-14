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
	@CliAvailabilityIndicator({ "constraints setup", "constraints equals", "constraints notEquals", "constraints contains", "constraints notContains", "constraints greater", "constraints smaller", "constraints rawExpression", "constraints removeRaw", "constraints update","constraints removeAll" })
	public boolean isCommandAvailable() {
		return operations.isCommandAvailable();
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param class 
	 * @param fields
	 */
	@CliCommand(value = "constraints equals", help = "Defines an equals constraint")
	public String equals(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption(key = "fieldlist", mandatory = true, help = "Fieldnames to compare against each other. Like \"fieldname1,fieldname2,...\""
		) String fieldlist,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf
	){
		return operations.annotateConstraintSimple(ConstraintType.EQUALS, javaType, fieldlist, message, applyIf);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param class 
	 * @param fields
	 */
	@CliCommand(value = "constraints notEquals", help = "Defines an notEquals constraint")
	public String notEquals(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption(key = "fieldlist", mandatory = true, help = "Fieldnames to compare against each other. Like \"fieldname1,fieldname2,...\""
		) String fieldlist,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf
	){
		return operations.annotateConstraintSimple(ConstraintType.NOTEQUALS, javaType, fieldlist, message, applyIf);
	}

	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param class 
	 * @param fields
	 */
	@CliCommand(value = "constraints contains", help = "Defines a constraint to check intersected strings")
	public String contains(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption(key = "field",	mandatory = true, help = "Fieldname is the main field to compare with the others in the fieldlist. (Annotation: \"<field>.contains(<fieldlist_element>)\")"
		) String field,
		@CliOption(key = "fieldlist", mandatory = true, help = "Fieldnames to compare against the main field. Like \"fieldname1,fieldname2,...\". (Annotation: \"<field>.contains(<fieldlist_element>)\")"
		) String fieldlist,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf
	){
		fieldlist = field + "," + fieldlist;
		return operations.annotateConstraintSimple(ConstraintType.CONTAINS, javaType, fieldlist, message, applyIf);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param class 
	 * @param fields
	 */
	@CliCommand(value = "constraints notContains", help = "Defines a constraint to check complemented strings")
	public String notContains(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption(key = "field",	mandatory = true, help = "Fieldname is the main field to compare with the others in the fieldlist. (Annotation: \"!<field>.contains(<fieldlist_element>)\")"
		) String field,
		@CliOption(key = "fieldlist", mandatory = true, help = "Fieldnames to compare against the main field. Like \"fieldname1,fieldname2,...\". (Annotation: \"!<field>.contains(<fieldlist_element>)\")"
		) String fieldlist,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf
	){
		fieldlist = field + "," + fieldlist;
		return operations.annotateConstraintSimple(ConstraintType.NOTCONTAINS, javaType, fieldlist, message, applyIf);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param class 
	 * @param fields
	 */
	@CliCommand(value = "constraints greater", help = "Defines a greater-then constraint")
	public String greater(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption(key = "field",	mandatory = true, help = "Fieldname is the main field to compare with the others in the fieldlist. (Annotation: \"<field> > <fieldlist_element>\")"
		) String field,
		@CliOption(key = "fieldlist", mandatory = true, help = "Fieldnames to compare against the main field. Like \"fieldname1,fieldname2,...\". (Annotation: \"<field> > <fieldlist_element>\")"
		) String fieldlist,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf
	){
		fieldlist = field + "," + fieldlist;
		return operations.annotateConstraintSimple(ConstraintType.GREATER, javaType, fieldlist, message, applyIf);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param class 
	 * @param fields
	 */
	@CliCommand(value = "constraints smaller", help = "Defines a smaller-than constraint")
	public String smaller(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption(key = "field",	mandatory = true, help = "Fieldname is the main field to compare with the others in the fieldlist. (Annotation: \"<field> < <fieldlist_element>\")"
		) String field,
		@CliOption(key = "fieldlist", mandatory = true, help = "Fieldnames to compare against the main field. Like \"fieldname1,fieldname2,...\". (Annotation: \"<field> < <fieldlist_element>\")"
		) String fieldlist,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf
	){
		fieldlist = field + "," + fieldlist;
		return operations.annotateConstraintSimple(ConstraintType.SMALLER, javaType, fieldlist, message, applyIf);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 * @param expression 
	 * @param class 
	 * @param message 
	 */
	@CliCommand(value = "constraints rawExpression", help = "Defines a constraint by a custom SpEL-Expression")
	public String rawExpression(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption( key = "expression", mandatory = true, help = "Raw expression to validate the annotation with."
		) String rawExpression,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf,
		@CliOption(	key = "helpers", mandatory = false, help = "Helpers Class"
		) JavaType helpers
	){
		return operations.annotateConstraintRaw(javaType, rawExpression, message, applyIf, helpers, AnnotationState.NEW);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 */
	@CliCommand(value = "constraints removeRaw", help = "Remove specific constraint annotation in class, with matching raw expression")
	public String removeRaw(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption( key = "expression", mandatory = true, help = "Raw expression to validate the annotation with."
		) String rawExpression
	){
		return operations.annotateConstraintRaw(javaType, rawExpression, "", "", null, AnnotationState.REMOVE);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 */
	@CliCommand(value = "constraints update", help = "Update specific constraint annotation in class, with matching expression")
	public String update(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) to apply this constraint annotation to"
		) JavaType javaType,
		@CliOption( key = "expression", mandatory = true, help = "Raw expression to validate the annotation with."
		) String rawExpression,
		@CliOption(	key = "message", mandatory = true, help = "Message text (String)"
		) String message,
		@CliOption(	key = "applyIf", mandatory = false, help = "ApplyIf SpEL-Expression (String)"
		) String applyIf,
		@CliOption(	key = "helpers", mandatory = false, help = "Helpers Class"
		) JavaType helpers
	){
		return operations.annotateConstraintRaw(javaType, rawExpression, message, applyIf, helpers, AnnotationState.UPDATE);
	}
	
	/**
	 * This method registers a command with the Roo shell. It also offers mandatory command attributes.
	 * 
	 */
	@CliCommand(value = "constraints removeAll", help = "Remove all constraint annotations in class")
	public void removeAll(
		@CliOption( key = "class", mandatory = true, help = "The class (e.g. from java type entity) where all annotations should be removed"
		) JavaType javaType
	) {
		operations.removeAllAnnoations(javaType);
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