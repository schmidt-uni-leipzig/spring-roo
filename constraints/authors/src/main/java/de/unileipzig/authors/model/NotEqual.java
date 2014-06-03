package de.unileipzig.authors.model;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

// these are required, and help document the validation to the
// validation engine. Note the validatedBy entry, which ties the
// annotation to a validator

@Documented
@Constraint(validatedBy = NotEqualValidator.class)
@Target({ TYPE, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
//@NotNull
public @interface NotEqual {
	String message() default "Attributes of classes should not be equal.";
	Class<?>[] groups() default { };
	Class<? extends Payload>[] payload() default {};
	
	String field() default "";
	String scope() default "";
}
