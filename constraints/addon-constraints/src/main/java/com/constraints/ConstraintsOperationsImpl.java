package com.constraints;

import java.util.ArrayList;
import java.util.Arrays;
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
//import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.springframework.uaa.client.util.Assert;
import org.w3c.dom.Element;

//import cz.jirutka.validator.spring.SpELAssert;
//import cz.jirutka.validator.spring.SpELAssertList;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
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
	public ArrayList<String> getValidFieldList(
		String string,
		JavaType javaType
	){
		// initialize list of valid fields or error message
		ArrayList<String> list = new ArrayList<String>();
		
		// check whether input is null or empty
		if (string == null || string.equals("")){
			list.add("Fields input is empty.");
			return list;
		}
		
		// prepare prefix of error message
		String error_message = "Field list is invalid \""+string+"\": ";
		
		// create temp list of input
		ArrayList<String> raw = new ArrayList<String>(
			Arrays.asList(
				string.split(",")
			)
		);
		
		// check fieldnames in detail
		for (int i=0; i<(raw.size()); i++){
			// get fieldname
			String fieldname = raw.get(i);
			
			// check for empty fieldname
			if (!fieldname.equals("")){
				
				// check whether field exists in class
				if (isFieldInClass(fieldname,javaType)){
					
					// check for doubles
					if (!list.contains(fieldname)){
						// add valid fieldname to list
						list.add(fieldname);
					}
					else{
						// clear previous fields in list, because field doesn't exists in class
						list.clear();
						// return error message
						list.add(error_message+"Field \""+ fieldname + "\" exists twice.");
						return list;
					}
				}
				else{
					// clear previous fields in list, because field doesn't exists in class
					list.clear();
					// return error message
					list.add(error_message+"Field \""+ fieldname + "\" doesn't exists in class \"" + javaType.getSimpleTypeName() + "\"");
					return list;
				}
			}
			else{
				// clear previous fields in list, because one element is empty
				list.clear();
				// return error message
				list.add(error_message+"No empty fieldnames allowed.");
				return list;
			}
		}
		
		// check whether list is empty or contains only one fieldname
		switch (list.size()) {
		case 0:
			// return error message
			list.add(error_message+"You need to name at least two valid fields.");
			return list;
		case 1:
			// clear one field in list, because its an invalid list
			list.clear();
			// return error message
			list.add(error_message+"You need more then just one field.");
			return list;
		default:
			// list is completely valid, no error message is needed
			// return valid list of fieldnames
			return list;
		}
	}
	
	/** {@inheritDoc} */
	public boolean isFieldInClass(String fieldname, JavaType javaType){
		// Obtain ClassOrInterfaceTypeDetails for this java type
		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);
		// return whether class(javaType) declares fieldname
		return existing.declaresField(new JavaSymbolName(fieldname));
	}
	
	/** {@inheritDoc} 
	 * @return */
	public String annotateConstraintRaw(
		JavaType javaType,
		String rawExpression,
		String message,
		String applyIf,
		JavaType helpers
	){
		// Use Roo's Assert type for null checks
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
				rooConstraintsAttributes.add(new ClassAttributeValue(new JavaSymbolName("helpers"), helpers)); // TODO check functionality
			}

			// Create inner annotation metadata
			AnnotationMetadataBuilder innerAnnotationBuilder = new AnnotationMetadataBuilder(innerSpELAssert, rooConstraintsAttributes);
			
			ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
			
			// Create JavaType instance for the add-ons trigger annotation of the list of annotations
			JavaType rooSpELAssertList = new JavaType("cz.jirutka.validator.spring.SpELAssertList");
			
			// List of inner annotations
			List<NestedAnnotationAttributeValue> annotationsList = new ArrayList<NestedAnnotationAttributeValue>();
			// AnnotationMetadataBuilder
			AnnotationMetadataBuilder annotationBuilder = null;				// NOTE: final entfernt
			
			// Test if the annotation already exists on the target type
			if (existingAnnotationMetaData == null){
				// Add inner annotations as attribute
				annotationsList.add(new NestedAnnotationAttributeValue(new JavaSymbolName("value"), innerAnnotationBuilder.build()));
				
				// Create NEW Annotation metadata for the List
				annotationBuilder = new AnnotationMetadataBuilder(rooSpELAssertList);
			}
			else{
				// Create Annotation metadata for the List from EXISTING Annotation
				annotationBuilder = new AnnotationMetadataBuilder(existingAnnotationMetaData);
				// Get all Attributes of Annotation
				Map<String, AnnotationAttributeValue<?>> annotationAttributes = annotationBuilder.getAttributes();
				// Get value attribute (object with value array of annotations)
				AnnotationAttributeValue<?> value = annotationAttributes.get("value");
				// Get list of annotations
				List<NestedAnnotationAttributeValue> oldAnnotationsList = (List<NestedAnnotationAttributeValue>) value.getValue();  //TODO check cast
				// Create new annotation value
				NestedAnnotationAttributeValue newAnnotation = new NestedAnnotationAttributeValue(new JavaSymbolName("value"), innerAnnotationBuilder.build());
				
				// Put old unmodifiable list elements(annotations) in new list
				for (NestedAnnotationAttributeValue oldAnnotation : oldAnnotationsList) {
					annotationsList.add(oldAnnotation);
				}
				
				// Check whether new annotation is NOT in old annotations list
				// (with oldAnnotationList, because its more understandable, CHANGE TO newAnnoationa List, IF not equal!!!)
				if (!containsAnnotation(oldAnnotationsList, newAnnotation)){
					// Remove old existing SpELAssertList annotation from class(javaType)
			    	classOrInterfaceTypeDetailsBuilder.removeAnnotation(rooSpELAssertList);
					
					// Then add new inner annotation to the list
					annotationsList.add(newAnnotation);
				}
				else{
					// Break method and return error message, that Constraint (value) already exists in class
					return "Annotation \""+ rawExpression + "\" already exists in class \"" + javaType.getSimpleTypeName() + "\"";
				}
			}
			
			// Add annotation list as attribute to the Annotation
			annotationBuilder.addAttribute(new ArrayAttributeValue<NestedAnnotationAttributeValue>(new JavaSymbolName("value"), annotationsList));
			
			// Add annotation to target type
			classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
			
			// Save changes to disk
			typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
		}
		return "Create Annotation with \"" + rawExpression + "\".";
	}
	
	// Check whether old annotations list contains the new annotation
	private boolean containsAnnotation(List<NestedAnnotationAttributeValue> oldAnnotationsList, NestedAnnotationAttributeValue newAnnotation){
		// Get value of new annotation
		Object newExpression = newAnnotation.getValue().getAttribute("value").getValue();
		
		// check old annotations, whether it contains the new annotation (only its value, rest could be equal with other constraints)
		for (NestedAnnotationAttributeValue oldAnnotation : oldAnnotationsList) {
			Object oldExpression = oldAnnotation.getValue().getAttribute("value").getValue();
			if (oldExpression.equals(newExpression)){
				return true;
			}
		}
		return false;
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
	
//	// Remove SpELAssert annotation from SpELAssertList annotation in class
//	public void removeAnnotation(JavaType javaType, String expression){
//
//		// Use Roo's Assert type for null checks
//		Validate.notNull(javaType, "Java type required");
//
//		// Obtain ClassOrInterfaceTypeDetails for this java type
//		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);
//
//		// Test if the annotation is present
//		if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), new JavaType("cz.jirutka.validator.spring.SpELAssertList")) != null) {
//	
//			ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
//	
//			JavaType rooTimestamp = new JavaType("cz.jirutka.validator.spring.SpELAssertList");
//	
//			// Add annotation to target type
//			classOrInterfaceTypeDetailsBuilder.removeAnnotation(rooTimestamp);
//	
//			// Save changes to disk
//			typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
//		}
//	}
	
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