package org.springframework.roo.addon.json;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.COLLECTION;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.SpringJavaType.REQUEST_MAPPING;
import static org.springframework.roo.model.RooJavaType.ROO_JSON;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata to be triggered by {@link RooJson} annotation
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */

public class JsonMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaType JSON_DESERIALIZER = new JavaType(
            "flexjson.JSONDeserializer");
    private static final JavaType JSON_SERIALIZER = new JavaType(
            "flexjson.JSONSerializer");

    private static final String PROVIDES_TYPE_STRING = JsonMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public static String createIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static JavaType getJavaType(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static LogicalPath getPath(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    private JsonAnnotationValues annotationValues;

    private String typeNamePlural;

    public JsonMetadata(final String identifier, final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String typeNamePlural,
            final JsonAnnotationValues annotationValues) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.notNull(annotationValues, "Annotation values required");
        Validate.notBlank(typeNamePlural, "Plural of the target type required");
        Validate.isTrue(
                isValid(identifier),
                "Metadata identification string '%s' does not appear to be a valid",
                identifier);

        if (!isValid()) {
            return;
        }

        this.annotationValues = annotationValues;
        this.typeNamePlural = typeNamePlural;

        builder.addMethod(getToJsonMethod(false));
        builder.addMethod(getToJsonMethod(true));
        builder.addMethod(getFromJsonMethod());
        builder.addMethod(getToJsonArrayMethod(false));
        builder.addMethod(getToJsonArrayMethod(true));
        builder.addMethod(getFromJsonArrayMethod());
        builder.addMethod(getSerializerMethod());
        
        builder.getImportRegistrationResolver().addImport(new JavaType("org.springframework.roo.addon.json.customizing.NullSerialization"));
        builder.getImportRegistrationResolver().addImport(new JavaType("org.springframework.roo.addon.json.customizing.NullSerializationException"));
        builder.getImportRegistrationResolver().addImport(new JavaType("org.springframework.roo.addon.json.customizing.NullSerializationAnnotation"));
        builder.getImportRegistrationResolver().addImport(new JavaType("org.springframework.roo.addon.json.customizing.NullSerializationTransformer"));
        builder.getImportRegistrationResolver().addImport(new JavaType("org.springframework.roo.addon.json.customizing.ShallowSerializationTransformer"));
        builder.getImportRegistrationResolver().addImport(new JavaType("org.springframework.roo.addon.json.RooJson"));
        builder.getImportRegistrationResolver().addImport(new JavaType("java.lang.reflect.Field"));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private MethodMetadataBuilder getFromJsonArrayMethod() {
        // Compute the relevant method name
        final JavaSymbolName methodName = getFromJsonArrayMethodName();
        if (methodName == null) {
            return null;
        }

        final JavaType parameterType = JavaType.STRING;
        if (governorHasMethod(methodName, parameterType)) {
            return null;
        }

        final String list = LIST.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
        final String arrayList = ARRAY_LIST.getNameIncludingTypeParameters(
                false, builder.getImportRegistrationResolver());
        final String bean = destination.getSimpleTypeName();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        final String deserializer = JSON_DESERIALIZER
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver());
        bodyBuilder.appendFormalLine("return new " + deserializer + "<" + list
                + "<" + bean + ">>()");
        if (annotationValues.isIso8601Dates()) {
            bodyBuilder
                    .appendFormalLine(".use(java.util.Date.class, "
                            + "new flexjson.transformer.DateTransformer(\"yyyy-MM-dd\"))");
        }
        bodyBuilder.appendFormalLine(".use(\"values\", " + bean
                + ".class).deserialize(json);");

        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName("json"));

        final JavaType collection = new JavaType(
                COLLECTION.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(destination));

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC | Modifier.STATIC, methodName,
                collection,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);
        methodBuilder.putCustomData(CustomDataJsonTags.FROM_JSON_ARRAY_METHOD,
                null);
        return methodBuilder;
    }

    public JavaSymbolName getFromJsonArrayMethodName() {
        final String methodLabel = annotationValues.getFromJsonArrayMethod();
        if (StringUtils.isBlank(methodLabel)) {
            return null;
        }

        return new JavaSymbolName(methodLabel.replace("<TypeNamePlural>",
                typeNamePlural));
    }

    private MethodMetadataBuilder getFromJsonMethod() {
        final JavaSymbolName methodName = getFromJsonMethodName();
        if (methodName == null) {
            return null;
        }

        final JavaType parameterType = JavaType.STRING;
        if (governorHasMethod(methodName, parameterType)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        final String deserializer = JSON_DESERIALIZER
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver());
        bodyBuilder.appendFormalLine("return new " + deserializer + "<"
                        + destination.getSimpleTypeName()
                        + ">()");
        if (annotationValues.isIso8601Dates()) {
            bodyBuilder
                    .appendFormalLine(".use(java.util.Date.class, "
                            + "new flexjson.transformer.DateTransformer(\"yyyy-MM-dd\"))");
        }
        bodyBuilder.appendFormalLine(".use(null, "
                + destination.getSimpleTypeName()
                + ".class).deserialize(json);");

        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName("json"));

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC | Modifier.STATIC, methodName,
                destination,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);
        methodBuilder.putCustomData(CustomDataJsonTags.FROM_JSON_METHOD, null);
        return methodBuilder;
    }

    public JavaSymbolName getFromJsonMethodName() {
        final String methodLabel = annotationValues.getFromJsonMethod();
        if (StringUtils.isBlank(methodLabel)) {
            return null;
        }

        // Compute the relevant method name
        return new JavaSymbolName(methodLabel.replace("<TypeName>",
                destination.getSimpleTypeName()));
    }

    private MethodMetadataBuilder getToJsonArrayMethod(boolean includeParams) {
        // Compute the relevant method name
        final JavaSymbolName methodName = getToJsonArrayMethodName();
        if (methodName == null) {
            return null;
        }

        final JavaType parameterType = new JavaType(Collection.class.getName(),
                0, DataType.TYPE, null, Arrays.asList(destination));

        // See if the type itself declared the method
        if (governorHasMethod(methodName, parameterType)) {
            return null;
        }

        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("collection"));

        final List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(parameterType);
        
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING));
        parameterNames.add(new JavaSymbolName("path"));

        if (includeParams) {
            parameterTypes.add(new AnnotatedJavaType(JavaType.STRING_ARRAY));
            parameterNames.add(new JavaSymbolName("fields"));
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return GetSerializer(path)");
        bodyBuilder.appendFormalLine(
                        (!includeParams ? "" : ".include(fields)")
                        + ".exclude(\"*.class\")"
                        + (annotationValues.isDeepSerialize() ? ".deepSerialize(collection)"
                                : ".serialize(collection)") + ";");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC | Modifier.STATIC, methodName, STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.putCustomData(CustomDataJsonTags.TO_JSON_ARRAY_METHOD,
                null);
        return methodBuilder;
    }

    public JavaSymbolName getToJsonArrayMethodName() {
        final String methodLabel = annotationValues.getToJsonArrayMethod();
        if (StringUtils.isBlank(methodLabel)) {
            return null;
        }
        return new JavaSymbolName(methodLabel);
    }

    private MethodMetadataBuilder getToJsonMethod(boolean includeParams) {
        // Compute the relevant method name
        final JavaSymbolName methodName = getToJsonMethodName();
        if (methodName == null) {
            return null;
        }

        // See if the type itself declared the method
        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return GetSerializer(path)");
        bodyBuilder.appendFormalLine(
                (!includeParams ? "" : ".include(fields)")
                + ".exclude(\"*.class\")"
                + (annotationValues.isDeepSerialize() ? ".deepSerialize(this)"
                        : ".serialize(this)") + ";");

        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING));
        parameterNames.add(new JavaSymbolName("path"));
        
        if (includeParams) {
            parameterTypes.add(new AnnotatedJavaType(JavaType.STRING_ARRAY));
            parameterNames.add(new JavaSymbolName("fields"));
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, STRING, parameterTypes,
                parameterNames, bodyBuilder);
        methodBuilder.putCustomData(CustomDataJsonTags.TO_JSON_METHOD, null);
        return methodBuilder;
    }

    public JavaSymbolName getToJsonMethodName() {
        final String methodLabel = annotationValues.getToJsonMethod();
        if (StringUtils.isBlank(methodLabel)) {
            return null;
        }
        return new JavaSymbolName(methodLabel);
    }
    
    private MethodMetadataBuilder getSerializerMethod() {
        // Compute the relevant method name
        final JavaSymbolName methodName = new JavaSymbolName("GetSerializer");
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING));
        parameterNames.add(new JavaSymbolName("path"));
        
        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        final String serializer = JSON_SERIALIZER
                .getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver());
        final String root = annotationValues.getRootName() != null
                && annotationValues.getRootName().length() > 0 ? ".rootName(\""
                + annotationValues.getRootName() + "\")" : "";
        bodyBuilder.appendFormalLine("JSONSerializer s = new " + serializer + "()" + root + ";");
        
        bodyBuilder.appendFormalLine("Field[] fields = " + destination.getSimpleTypeName() + ".class.getDeclaredFields();");
        bodyBuilder.appendFormalLine("for(Field field : fields){");
		bodyBuilder.appendFormalLine("NullSerializationAnnotation anno = field.getAnnotation(NullSerializationAnnotation.class);");
        bodyBuilder.appendFormalLine("RooJson rooAnno = field.getAnnotation(RooJson.class);");
        bodyBuilder.appendFormalLine("if(anno != null){");
        bodyBuilder.appendFormalLine("try{");
        bodyBuilder.appendFormalLine("if(anno.nullSerialization() == NullSerialization.DEFAULT)");
        bodyBuilder.appendFormalLine("s = s.transform(new NullSerializationTransformer(anno.nullSerialization(), anno.defaultValue()), field.getName().toString().trim().toLowerCase());");
        bodyBuilder.appendFormalLine("else");
        bodyBuilder.appendFormalLine("s = s.transform(new NullSerializationTransformer(anno.nullSerialization()), field.getName().toString().trim().toLowerCase());");
        bodyBuilder.appendFormalLine("} catch (NullSerializationException e) {");
        bodyBuilder.appendFormalLine("e.printStackTrace();");
        bodyBuilder.appendFormalLine("} }");
        if(!annotationValues.isDeepSerialize()) //not!!
        {
	        bodyBuilder.appendFormalLine("if(anno != null)");
	        bodyBuilder.appendFormalLine("s = s.transform(new ShallowSerializationTransformer(path), Object.class);");
        }
        bodyBuilder.appendFormalLine("}");
        
        
        if (annotationValues.isIso8601Dates()) { 
            bodyBuilder
                    .appendFormalLine("s = s.transform("
                    + "new flexjson.transformer.DateTransformer"
                    + "(\"yyyy-MM-dd\"), java.util.Date.class);");
        }
        bodyBuilder.appendFormalLine("return s;");
        
        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE | Modifier.STATIC, methodName, JSON_SERIALIZER, parameterTypes,
                parameterNames, bodyBuilder);
        methodBuilder.putCustomData(CustomDataJsonTags.TO_JSON_METHOD, null);
        return methodBuilder;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }
}
