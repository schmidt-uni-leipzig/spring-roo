package org.springframework.roo.addon.json;

import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of addon-json operations interface.
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */
@Component
@Service
public class JsonOperationsImpl implements JsonOperations {

    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;

    public void annotateAll() {
        annotateAll(false, false);
    }

    public void annotateAll(final boolean deepSerialize) {
        annotateAll(deepSerialize, false);
    }

    public void annotateAll(final boolean deepSerialize,
            final boolean iso8601Dates) {
        for (final JavaType type : typeLocationService
                .findTypesWithAnnotation(ROO_JAVA_BEAN)) {
            annotateType(type, "", deepSerialize, iso8601Dates);
        }
    }

    public void annotateType(final JavaType javaType, final String rootName) {
        annotateType(javaType, rootName, false);
    }

    public void annotateType(final JavaType javaType, final String rootName,
            final boolean deepSerialize) {
        annotateType(javaType, rootName, false, false);
    }

    public void annotateType(final JavaType javaType, final String rootName, final boolean deepSerialize, final boolean iso8601Dates) {
        Validate.notNull(javaType, "Java type required");

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(javaType);
        if (cid == null) {
            throw new IllegalArgumentException("Cannot locate source for '"
                    + javaType.getFullyQualifiedTypeName() + "'");
        }

        if (MemberFindingUtils.getAnnotationOfType(cid.getAnnotations(), RooJavaType.ROO_JSON) == null) 
        {
            final AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(RooJavaType.ROO_JSON);
            if (rootName != null && rootName.length() > 0) {
                annotationBuilder.addStringAttribute("rootName", rootName);
            }
            if (deepSerialize) {
                annotationBuilder.addBooleanAttribute("deepSerialize", true);
            }
            if (iso8601Dates) {
                annotationBuilder.addBooleanAttribute("iso8601Dates", true);
            }
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    cid);
            cidBuilder.addAnnotation(annotationBuilder);
            typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
        }
    }
    
    public void nullSerializationOption(final JavaType javaType, final String nullSerialization, final String[] fieldNames, final String[] defaultValues) throws Exception
    {
        final ClassOrInterfaceTypeDetails cid = typeLocationService.getTypeDetails(javaType);
        
        if (cid == null) 
            throw new IllegalArgumentException("Cannot locate source for '"+ javaType.getFullyQualifiedTypeName() + "'");
        
        AnnotationMetadata rooJsonAnno = MemberFindingUtils.getAnnotationOfType(cid.getAnnotations(), RooJavaType.ROO_JSON);
    	if(rooJsonAnno == null)
    		throw new IllegalArgumentException("Type not annotated: '"+ javaType.getFullyQualifiedTypeName() + "'\n annotate type with '@RooJson' first");
    	
    	//AnnotationMetadataBuilder rooJsonAnnoBuilder = new AnnotationMetadataBuilder(rooJsonAnno);
                
    	JavaType nullAnno = new JavaType("org.springframework.roo.addon.json.customizing.NullSerializationAnnotation");
    	JavaType nullEnum = new JavaType("org.springframework.roo.addon.json.customizing.NullSerialization");
        AnnotationMetadataBuilder nullAnnoBuilder = new AnnotationMetadataBuilder();
        nullAnnoBuilder.setAnnotationType(nullAnno);
        nullAnnoBuilder.addEnumAttribute("nullSerialization", nullEnum, nullSerialization);

        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(cid);
        
        for(int i =0;i<fieldNames.length;i++)
        {
	    	JavaSymbolName name = new JavaSymbolName(fieldNames[i]);
	        FieldMetadata field = typeLocationService.getTypeDetails(javaType).getDeclaredField(name);
	        Validate.notNull(field, "Field not found: " + fieldNames[i]);
	        List<FieldMetadataBuilder> fieldBuilders = cidBuilder.getDeclaredFields();
	        for(int j =0; j < fieldBuilders.size();j++)
	        {
	        	if(fieldBuilders.get(j).getFieldName().toString().trim().toLowerCase().equals(field.getFieldName().toString().trim().toLowerCase()))
	        	{
	        		if(field.getAnnotation(nullAnno) != null)
	        			fieldBuilders.get(j).removeAnnotation(nullAnno);
	                if(nullSerialization.equals("DEFAULT"))
	                	nullAnnoBuilder.addStringAttribute("defaultValue", defaultValues[i]);
	        		fieldBuilders.get(j).addAnnotation(nullAnnoBuilder);
	        		
//	        		String targetName = field.getFieldName().toString().trim().toLowerCase();
//	        		if(defaultValues != null)
//	        			completeJsonAnnotation(rooJsonAnnoBuilder, targetName, nullSerialization, defaultValues[i]);
//	        		else
//	        			completeJsonAnnotation(rooJsonAnnoBuilder, targetName, nullSerialization, null);
	        		break;
	        	}
	        }
        }
//        cidBuilder.removeAnnotation(RooJavaType.ROO_JSON);
//        cidBuilder.addAnnotation(rooJsonAnnoBuilder);
        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }
    
    private void completeJsonAnnotation(AnnotationMetadataBuilder rooJsonAnnoBuilder, final String targetName, final String nullSerialization, final String defaultValue)
    {
    	String removeProps = rooJsonAnnoBuilder.getAttributes().get("removeNullProps") == null ? null : rooJsonAnnoBuilder.getAttributes().get("removeNullProps").getValue().toString();
    	String emptyProps = rooJsonAnnoBuilder.getAttributes().get("emptyNullProps") == null ? null : rooJsonAnnoBuilder.getAttributes().get("emptyNullProps").getValue().toString();
    	String nullProps = rooJsonAnnoBuilder.getAttributes().get("nullNullProps") == null ? null : rooJsonAnnoBuilder.getAttributes().get("nullNullProps").getValue().toString();
    	String defaultProps = rooJsonAnnoBuilder.getAttributes().get("defaultNullProps") == null ? null : rooJsonAnnoBuilder.getAttributes().get("defaultNullProps").getValue().toString();
    	
		if(removeProps != null)
			removeProps = removeProps.replace(" ", "").replace("," + targetName, "").replace(targetName,  "");
		if(emptyProps != null)
			emptyProps = emptyProps.replace(" ", "").replace("," + targetName, "").replace(targetName,  "");
		if(nullProps != null)
			nullProps = nullProps.replace(" ", "").replace("," + targetName, "").replace(targetName,  "");
		if(defaultProps != null)
			defaultProps = defaultProps.replace(" ", "").replace("," + targetName, "").replace(targetName,  "");
    	
    	if(nullSerialization.equals("REMOVE"))
    	{
    		if(removeProps == null || removeProps.length()==0)
    			removeProps = targetName;
    		else
    			removeProps += "," + targetName;
    	}
    	if(nullSerialization.equals("EMPTY"))
    	{
    		if(emptyProps == null || emptyProps.length()==0)
    			emptyProps = targetName;
    		else
    			emptyProps += "," + targetName;
    	}
    	if(nullSerialization.equals("NULL"))
    	{
    		if(nullProps == null || nullProps.length()==0)
    			nullProps = targetName;
    		else
    			nullProps += "," + targetName;
    	}
    	if(nullSerialization.equals("DEFAULT") && defaultValue != null)
    	{
    		if(defaultProps == null || defaultProps.length()==0)
    			defaultProps = targetName;
    		else
    			defaultProps += "," + targetName;
    	}
    	
		if(removeProps != null)
			rooJsonAnnoBuilder.addStringAttribute("removeNullProps", removeProps);
		if(emptyProps != null)
			rooJsonAnnoBuilder.addStringAttribute("emptyNullProps", emptyProps);
		if(nullProps != null)
			rooJsonAnnoBuilder.addStringAttribute("nullNullProps", nullProps);
		if(defaultProps != null)
			rooJsonAnnoBuilder.addStringAttribute("defaultNullProps", defaultProps);
    	
    }

    public boolean isJsonInstallationPossible() {
        return projectOperations.isFocusedProjectAvailable();
    }

	@Override
	public void addAdditionalDependencies() 
	{
        List<Dependency> dependencies = new ArrayList<Dependency>(); 
        InputStream input = getClass().getResourceAsStream(this.getClass().getResource("configuration.xml").getPath());
        Document configuration = XmlUtils.readXml(input);
        for (Element dependencyElement :  XmlUtils.findElements("/configuration/dependencies/dependency", configuration.getDocumentElement())) 
        { 
        	dependencies.add(new Dependency(dependencyElement)); 
        } 
        projectOperations.addDependencies("", dependencies); 

        List<Element> repositories = XmlUtils.findElements("/configuration/repositories/repository", configuration.getDocumentElement()); 
        for (Element repositoryElement : repositories) 
        {
        	Repository repository = new Repository(repositoryElement); 
            projectOperations.addRepository(projectOperations.getFocusedModuleName(),  repository); 
        } 
	}
}