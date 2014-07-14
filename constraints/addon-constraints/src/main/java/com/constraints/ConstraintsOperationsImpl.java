package com.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.NestedAnnotationAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
// Use these Apache Felix annotations to register your commands class in the Roo container
@Component
@Service
public class ConstraintsOperationsImpl implements ConstraintsOperations {
	/**
	 * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
	 */
	@Reference private ProjectOperations projectOperations;

	/**
	 * Use TypeLocationService to find types which are annotated with a given annotation in the project
	 */
	@Reference private TypeLocationService typeLocationService;
	
	/**
	 * Use TypeManagementService to change types
	 */
	@Reference private TypeManagementService typeManagementService;

	/** {@inheritDoc} */
	public boolean isCommandAvailable() {
		// Check if a project has been created
		return projectOperations.isFocusedProjectAvailable();
	}
	
	/** {@inheritDoc} */
	public String annotateConstraintSingle(
		ConstraintType constraintType,
		JavaType javaType,
		String field_subject,
		String field_object,
		String message,
		String applyIf,
		String newline,
		String message_return
	){
		// Prepare Annotation Expression
		String expression = "";
		
		// Create expression
		switch (constraintType) {
			case EQUALS:
				expression = field_subject+".equals("+field_object+")";
			break;
			case NOTEQUALS:
				expression = "!"+field_subject+".equals("+field_object+")";
			break;
			case CONTAINS:
				expression = field_subject+".contains("+field_object+")";
			break;
			case NOTCONTAINS:
				expression = "!"+field_subject+".contains("+field_object+")";
			break;
			case GREATER:
				expression = field_subject+" > "+field_object;
				break;
			case SMALLER:
				expression = field_subject+" < "+field_object;
			break;
			default:
				return "Constraint type invalid: " + constraintType.toString();
		}
		//Bind message as shell feedback.
		return message_return + newline + annotateConstraintRaw(javaType, expression, message, applyIf, null, AnnotationState.NEW);
	}
	/** {@inheritDoc} */
	public String annotateConstraintSimple(
		ConstraintType constraintType,
		JavaType javaType,
		String fieldlist,
		String message,
		String applyIf
	){
		// Use Roo's Assert type for null checks
		Validate.notNull(constraintType, "Constraint type is required");
		Validate.notNull(javaType, "Parameter --class is required");
		Validate.notNull(fieldlist, "Parameter --expression is required");
		Validate.notNull(message, "Parameter --message is required");
		
		// Prepare Annotation Type
		boolean crossproduct;
		
		// Appraise crossproduct by constraint type
		switch (constraintType) {
			case EQUALS: case NOTEQUALS:
				crossproduct = true;
			break;
			case CONTAINS: case NOTCONTAINS: case GREATER: case SMALLER:
				crossproduct = false;
			break;
			default:
				crossproduct = false;
		}
		
		// Prepare message_return string
		String message_return = "";
				
		// Prepare newline
		String newline = "";
		
		// Split shell string for list processing
		ArrayList<String> list = getValidFieldList(fieldlist,javaType);
		
		// Check whether list contains two or more valid fieldnames to create annotation(s)
		if(list.size()>1){
		
			//Apply different list processing by "crossproduct"
			if(crossproduct == true){
			
				// Add all annotation combinations of fieldnames
				for (int i=0; i<(list.size()-1); i++){
				
					for (int j=i+1; j<list.size(); j++){
						
						// Perform annotation and add information message to "return_message"
						message_return = annotateConstraintSingle(constraintType, javaType, list.get(i), list.get(j), message, applyIf, newline, message_return);
						
						// Add line break (after first annotation)
						newline = "\n";
					}
				}
			}else{
				
				// Add all annotation combinations of fieldnames
				for (int i=1; i<(list.size()); i++){
					
					// Perform annotation and add information message to return_message
					message_return = annotateConstraintSingle(constraintType, javaType, list.get(0), list.get(i), message, applyIf, newline, message_return);
				
					// Add line break (after first annotation)
					newline = "\n";
				}
			}
		}else{
			
			// Invalid input or list contains any error message
			message_return = list.get(0);
		}
		
		return message_return;
	}
	
	/** {@inheritDoc} */
	public ArrayList<String> getValidFieldList(
		String string,
		JavaType javaType
	){
		// Initialize list of valid fields or error message
		ArrayList<String> list = new ArrayList<String>();
		
		// Check whether input is null or empty
		if (string == null || string.equals("")){
			list.add("--fields input is empty.");
			return list;
		}
		
		// Prepare prefix of error message
		String error_message = "Field list is invalid \""+string+"\": ";
		
		// Create temp list of input
		ArrayList<String> raw = new ArrayList<String>(
			Arrays.asList(
				string.split(",")
			)
		);
		
		// Check fieldnames in detail
		for (int i=0; i<(raw.size()); i++){
			// Cet fieldname
			String fieldname = raw.get(i);
			
			// Check for empty fieldname
			if (!fieldname.equals("")){
				
				// Check whether field exists in class
				if (isFieldInClass(fieldname,javaType)){
					
					// Check for doubles
					if (!list.contains(fieldname)){
					
						// Add valid fieldname to list
						list.add(fieldname);
					}
					else{
					
						// Clear previous declared fields in list if field doesn't exists in class
						list.clear();
						
						// Return error message
						list.add(error_message+"Field \""+ fieldname + "\" exists twice.");
						return list;
					}
				}
				else{
				
					// Clear previous declared fields in list if field doesn't exists in class
					list.clear();
					
					// Return error message
					list.add(error_message+"Field \""+ fieldname + "\" doesn't exists in class \"" + javaType.getSimpleTypeName() + "\"");
					return list;
				}
			}
			else{
			
				// Clear previous declared fields in list if field doesn't exists in class
				list.clear();
				
				// Return error message
				list.add(error_message+"No empty fieldnames allowed.");
				
				return list;
			}
		}
		
		// Check whether list is empty or contains only one fieldname
		switch (list.size()) {
		case 0:
		
			// Return error message
			list.add(error_message+"You need to name at least two valid fields.");
			return list;
		case 1:
		
			// Clear previous declared fields in list if only one was passed
			list.clear();
			
			// Return error message
			list.add(error_message+"You need more then just one field.");
			
			return list;
		default:
		
			// Entire list is valid so no error message is needed
			// Return valid list of fieldnames
			return list;
		}
	}
	
	/** {@inheritDoc} */
	public boolean isFieldInClass(String fieldname, JavaType javaType){
	
		// Obtain ClassOrInterfaceTypeDetails for this java type
		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);
		
		// Return whether class(javaType) declares fieldname
		return existing.declaresField(new JavaSymbolName(fieldname));
	}
	
	/** {@inheritDoc} 
	 * @return */
	public String annotateConstraintRaw(
		JavaType javaType,
		String rawExpression,
		String message,
		String applyIf,
		JavaType helpers,
		AnnotationState state
	){
		// Use Roo's a ssert type for null checks
		Validate.notNull(javaType, "Parameter --class is required");
		Validate.notNull(rawExpression, "Parameter --expression is required");
		Validate.notNull(message, "Parameter --message is required");

		// Obtain ClassOrInterfaceTypeDetails for this java type
		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

		// Test if the annotation already exists on the target type and get existing annotation Metadata
		JavaType outerSpELAssertList = new JavaType("cz.jirutka.validator.spring.SpELAssertList");
		AnnotationMetadata existingAnnotationMetaData = MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), outerSpELAssertList);
		
		
		// Test if the javaType exists
		if (existing != null){
			
			// Create JavaType instance for the add-ons trigger annotation of the inner annotation
			JavaType innerSpELAssert = new JavaType("cz.jirutka.validator.spring.SpELAssert");
			
			// Add parameters to the new inner annotation
			final List<AnnotationAttributeValue<?>> rooConstraintsAttributes = new ArrayList<AnnotationAttributeValue<?>>();
			rooConstraintsAttributes.add(new StringAttributeValue(new JavaSymbolName("value"), rawExpression));
			rooConstraintsAttributes.add(new StringAttributeValue(new JavaSymbolName("message"), message));
			
			if (applyIf != null){
				rooConstraintsAttributes.add(new StringAttributeValue(new JavaSymbolName("applyIf"), applyIf));
			}
			if (helpers != null){
				rooConstraintsAttributes.add(new ClassAttributeValue(new JavaSymbolName("helpers"), helpers));
			}

			// Create inner annotation metadata
			AnnotationMetadataBuilder innerAnnotationBuilder = new AnnotationMetadataBuilder(innerSpELAssert, rooConstraintsAttributes);
			
			ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
			
			// Create JavaType instance for the add-ons trigger annotation of the list of annotations
			JavaType rooSpELAssertList = new JavaType("cz.jirutka.validator.spring.SpELAssertList");
			
			// List of inner annotations
			List<NestedAnnotationAttributeValue> newAnnotationsList = new ArrayList<NestedAnnotationAttributeValue>();
			
			// AnnotationMetadataBuilder
			AnnotationMetadataBuilder annotationBuilder = null; // Note: final removed
			
			// Test if the annotation already exists on the target type
			if (existingAnnotationMetaData == null){
			
				// Add inner annotations as attribute
				newAnnotationsList.add(new NestedAnnotationAttributeValue(new JavaSymbolName("value"), innerAnnotationBuilder.build()));
				
				// Create new annotation metadata for the list
				annotationBuilder = new AnnotationMetadataBuilder(rooSpELAssertList);
			}
			else{
				// Create Annotation metadata for the List from existing annotation
				annotationBuilder = new AnnotationMetadataBuilder(existingAnnotationMetaData);

				// Get list of old annotations
				List<NestedAnnotationAttributeValue> oldAnnotationsList = getOldAnnotationsList(annotationBuilder);
				
				// Create new annotation value
				NestedAnnotationAttributeValue newAnnotation = new NestedAnnotationAttributeValue(new JavaSymbolName("value"), innerAnnotationBuilder.build());
				
				switch ( state )
				{
				case NEW:
					// Check whether new annotation is not in old annotations list
					if (!containsAnnotation(oldAnnotationsList, newAnnotation)){
					
						// Remove old existing SpELAssertList annotation from class(javaType)
						classOrInterfaceTypeDetailsBuilder.removeAnnotation(rooSpELAssertList);
						newAnnotationsList = oldAnnotationsList;
						
						// Then add new inner annotation to the list
						newAnnotationsList.add(newAnnotation);
					}
					else{
						// Break method and return error message, that constraint (value) already exists in class
						return "Annotation \""+ rawExpression + "\" already exists in class \"" + javaType.getSimpleTypeName() + "\"";
					}
					break;
				case UPDATE:
					// Check whether new annotation is ALREADY in old annotations list
					if (containsAnnotation(oldAnnotationsList, newAnnotation)){
						// Remove old existing SpELAssertList annotation from class(javaType)
						classOrInterfaceTypeDetailsBuilder.removeAnnotation(rooSpELAssertList);
						
						// remove old annotation from oldAnnotationsList
						/*
						for (NestedAnnotationAttributeValue oldAnnotation : oldAnnotationsList) {
							if (equalsAnnotation(oldAnnotation, newAnnotation)){
								oldAnnotationsList.remove(oldAnnotation);
							}
						}
						*/
						
						for (Iterator<?> iterator = oldAnnotationsList.iterator(); iterator.hasNext();) {
							NestedAnnotationAttributeValue oldAnnotation = (NestedAnnotationAttributeValue) iterator.next();
							if (equalsAnnotation(oldAnnotation, newAnnotation)){
								iterator.remove();
							}
						}
						
						newAnnotationsList = oldAnnotationsList;
						
						// Then add new inner annotation to the list
						newAnnotationsList.add(newAnnotation);
					}
					else{
					
						// Break method and return error message, that constraint (value) already exists in class
						return "Annotation \""+ rawExpression + "\" doesn't exists in class \"" + javaType.getSimpleTypeName() + "\"";
					}
					break;
				case REMOVE:
					// Check whether new annotation is already in old annotations list
					if (containsAnnotation(oldAnnotationsList, newAnnotation)){
					
						// Remove old existing SpELAssertList annotation from class(javaType)
						classOrInterfaceTypeDetailsBuilder.removeAnnotation(rooSpELAssertList);
						
						// remove old annotation from oldAnnotationsList
						for (Iterator<?> iterator = oldAnnotationsList.iterator(); iterator.hasNext();) {
							NestedAnnotationAttributeValue oldAnnotation = (NestedAnnotationAttributeValue) iterator.next();
							if (equalsAnnotation(oldAnnotation, newAnnotation)){
								iterator.remove();
							}
						}
						
						newAnnotationsList = oldAnnotationsList;
					}
					else{
						// Break method and return error message, that Constraint (value) already exists in class
						return "Annotation \""+ rawExpression + "\" doesn't exists in class \"" + javaType.getSimpleTypeName() + "\"";
					}
					break;
				}
				
			}
			
			// Add annotation list as attribute to the Annotation
			annotationBuilder.addAttribute(new ArrayAttributeValue<NestedAnnotationAttributeValue>(new JavaSymbolName("value"), newAnnotationsList));
			
			// Add annotation to target type
			classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
			
			// Save changes to disk
			typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
		}
		
		switch ( state )
		{
		case NEW:
			return "Create annotation with \"" + rawExpression + "\".";
		case UPDATE:
			return "Update annotation with \"" + rawExpression + "\".";
		case REMOVE:
			return "Remove annotation with \"" + rawExpression + "\".";
		default:
			return "Nothing, because invalid state identifier: " + state.toString();
		}
	}
	
	private List<NestedAnnotationAttributeValue> getOldAnnotationsList(AnnotationMetadataBuilder annotationBuilder){
		
		// Get all attributes of annotation
		Map<String, AnnotationAttributeValue<?>> annotationAttributes = annotationBuilder.getAttributes();
		
		// Get value attribute (object with value array of annotations)
		AnnotationAttributeValue<?> value = annotationAttributes.get("value");
		
		// Get list of annotations
		List<NestedAnnotationAttributeValue> oldAnnotationsList = (List<NestedAnnotationAttributeValue>) value.getValue();
		
		// List of modifiable annotations
		List<NestedAnnotationAttributeValue> annotationsList = new ArrayList<NestedAnnotationAttributeValue>();
		
		// Put old unmodifiable list elements(annotations) in new list
		for (NestedAnnotationAttributeValue oldAnnotation : oldAnnotationsList) {
			annotationsList.add(oldAnnotation);
		}
		
		return annotationsList;
	}
	
	// Check whether old annotations list contains the new annotation
	private boolean containsAnnotation(List<NestedAnnotationAttributeValue> oldAnnotationsList, NestedAnnotationAttributeValue newAnnotation){
		
		// check old annotations, whether it contains the new annotation (only its value, rest could be equal with other constraints)
		for (NestedAnnotationAttributeValue oldAnnotation : oldAnnotationsList) {
			if (equalsAnnotation(oldAnnotation, newAnnotation)){
				return true;
			}
		}
		return false;
	}
	
	// Check whether old annotations equals the new annotation
	private boolean equalsAnnotation(NestedAnnotationAttributeValue oldAnnotation, NestedAnnotationAttributeValue newAnnotation){
	
		// Get value of old annotation
		Object oldExpression = oldAnnotation.getValue().getAttribute("value").getValue();
		// Get value of new annotation
		Object newExpression = newAnnotation.getValue().getAttribute("value").getValue();
		
		// check old annotation, whether it is equal to the new annotation (only its value, rest could be equal with other constraints)
		if (oldExpression.equals(newExpression)){
			return true;
		}
		else{
			return false;
		}
	}
	
	/** {@inheritDoc} */
	public void removeAllAnnoations(JavaType javaType){
		// Use Roo's Assert type for null checks
		Validate.notNull(javaType, "Java type required");

		// Obtain ClassOrInterfaceTypeDetails for this java type
		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

		// Test if the annotation is present
		if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), new JavaType("cz.jirutka.validator.spring.SpELAssertList")) != null) {
	
			ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
	
			JavaType rooTimestamp = new JavaType("cz.jirutka.validator.spring.SpELAssertList");
	
			// Add annotation to target type
			classOrInterfaceTypeDetailsBuilder.removeAnnotation(rooTimestamp);
	
			// Save changes to disk
			typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
		}
	}
	
	/** {@inheritDoc} */
	public void setup() {
		// Install the add-on Google code repository needed to get the annotation 
		projectOperations.addRepository("", new Repository("Constraints Roo add-on repository", "Constraints Roo add-on repository", "https://constraints-addon.googlecode.com/svn/repo"));
		
		List<Dependency> dependencies = new ArrayList<Dependency>();
		
		// Install the dependency on the add-on jar (
		dependencies.add(new Dependency("com.constraints", "com.constraints", "0.1.0.BUILD-SNAPSHOT", DependencyType.JAR, DependencyScope.PROVIDED));
		
		// Install dependencies defined in external XML file
		for (Element dependencyElement : XmlUtils.findElements("/configuration/batch/dependencies/dependency", XmlUtils.getConfiguration(getClass()))) {
			dependencies.add(new Dependency(dependencyElement));
		}

		// Add all new dependencies to pom.xml
		projectOperations.addDependencies("", dependencies);
	}
}