package com.constraints;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
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
	public void annotateConstraintRaw(
		JavaType paramClass,
		String rawExpression,
		String message,
		String applyIf
	){
		// Use Roo's Assert type for null checks
		Validate.notNull(paramClass, "Parameter --class is required");
		Validate.notNull(rawExpression, "Parameter --expression is required");
		Validate.notNull(message, "Parameter --message is required");

		// Obtain ClassOrInterfaceTypeDetails for this java type
		ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(paramClass);

		// Test if the annotation already exists on the target type
		if (
			existing != null 
			&& MemberFindingUtils.getAnnotationOfType(
					existing.getAnnotations(), 
					new JavaType("cz.jirutka.validator.spring.SpELAssertList")
				) == null
			){
			ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);
			
			// Create JavaType instance for the add-ons trigger annotation
			JavaType rooSpELAssertList = new JavaType("cz.jirutka.validator.spring.SpELAssertList");//"cz.jirutka.validator.spring.SpELAssert");

			// List of Annotations
			List<NestedAnnotationAttributeValue> annotationList = new ArrayList<NestedAnnotationAttributeValue>(1);
			
			
			
			
			// Create JavaType instance for the add-ons trigger annotation
			JavaType rooSpELAssert = new JavaType("cz.jirutka.validator.spring.SpELAssert");
			
			// Add parameters to the annotation
			final List<AnnotationAttributeValue<?>> rooConstraintsAttributes = new ArrayList<AnnotationAttributeValue<?>>();
			rooConstraintsAttributes.add(new StringAttributeValue(new JavaSymbolName("value"), rawExpression));
			rooConstraintsAttributes.add(new StringAttributeValue(new JavaSymbolName("message"), message));
			if (applyIf != null){
				rooConstraintsAttributes.add(new StringAttributeValue(new JavaSymbolName("applyIf"), applyIf));
			}

			// Create inner annotation metadata
			AnnotationMetadataBuilder innerAnnotationBuilder = new AnnotationMetadataBuilder(rooSpELAssert, rooConstraintsAttributes);
			
			
			
			// List attribute with all inner annotations
			annotationList.add(new NestedAnnotationAttributeValue(new JavaSymbolName("value"), innerAnnotationBuilder.build()));
			
			// Create Annotation metadata for the List
			final AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(rooSpELAssertList);
			annotationBuilder.addAttribute(new ArrayAttributeValue<NestedAnnotationAttributeValue>(new JavaSymbolName("value"), annotationList));
			
			// Add annotation to target type
			classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
			
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