package de.unileipzig.authors.model;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.AssertTrue;
import org.apache.log4j.Logger;

public class ConstraintTemplateValidator implements ConstraintValidator<ConstraintTemplate, String> {
	
	private static Logger log = Logger.getLogger(ConstraintTemplateValidator.class);
	private String parameter;

	public void initialize(ConstraintTemplate constraintAnnotation) {
		parameter = constraintAnnotation.parameter();
	}

	@AssertTrue
	public boolean isValid(String attribute, ConstraintValidatorContext context) {

		if (parameter.equals("false")){
			return false;
		}
		else{
			return true;
		}
	}
}