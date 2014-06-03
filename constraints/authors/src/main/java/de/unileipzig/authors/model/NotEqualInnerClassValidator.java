package de.unileipzig.authors.model;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.AssertTrue;

import org.apache.log4j.Logger;
import org.apache.commons.beanutils.BeanUtils;
//import org.apache.catalina.core.ApplicationContext;

//import org.springframework.context.ApplicationContext;
//import org.springframework.beans.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NotEqualInnerClassValidator implements ConstraintValidator<NotEqualInnerClass, Object> {
	
	private static Logger log = Logger.getLogger(NotEqualInnerClassValidator.class);
	private String field1;
	private String field2;

	public void initialize(NotEqualInnerClass constraintAnnotation) {
		field1 = constraintAnnotation.field1();
		field2 = constraintAnnotation.field2();
	}

	@AssertTrue
	public boolean isValid(Object object, ConstraintValidatorContext context) {
		
		try
        {
			final Object firstObj = BeanUtils.getProperty(object, field1);
			final Object secondObj = BeanUtils.getProperty(object, field2);
			
			return !firstObj.equals(secondObj);
			
        }
        catch (final Exception ignore)
        {
        	return true;
        }
	}
}