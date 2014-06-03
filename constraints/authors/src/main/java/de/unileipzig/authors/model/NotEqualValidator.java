package de.unileipzig.authors.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.AssertTrue;

import org.apache.log4j.Logger;
import org.apache.commons.beanutils.BeanUtils;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * Ist eher ein Unique Validator auf mehrere Felder unterschiedlicher Klassen!!!
 * Später umbenennen, falls noch benötigt.
 *
 */
public class NotEqualValidator implements ConstraintValidator<NotEqual, Object> {
	
	private static Logger log = Logger.getLogger(NotEqualValidator.class);
	private String field;
	private String scope;
	
	private static Map<String, List<Object>> scopes = new HashMap<String, List<Object>>();	

	public void initialize(NotEqual constraintAnnotation) {
		field = constraintAnnotation.field();
		scope = constraintAnnotation.scope();
	}

	@AssertTrue
	public boolean isValid(Object object, ConstraintValidatorContext context) {

		List<Object> scopeObjects = scopes.get(scope);
		boolean notEqual = true;
		
		// Object zum Feldnamen holen
		Object newField;
		try
        {
			newField = BeanUtils.getProperty(object, field);
        }
        catch (final Exception ignore)
        {
        	// Falls Feld nicht vorhanden
        	return true;
        }
		
		// Falls Feld null
		if (newField == null){
			return true; // FEHLER falls true: siehe unten
		}
		
		// Wenn nicht im Scope bisher hinterlegt, dann neue Liste dafür erstellen und true zurückgeben
		if (scopeObjects == null){
			scopeObjects = new ArrayList<Object>();
			scopeObjects.add(newField);
			scopes.put(scope, scopeObjects);
			notEqual = true;
		}
		else{
			// Wenn etwas im Scope, dann vergleichen ob gleich
			for (Object otherObjectsInScope : scopeObjects) {
				notEqual = !newField.equals(otherObjectsInScope);
				
				// Falls gleiches Feld gefunden, mit notEqual=false Schleife verlassen
				if (notEqual == false){
					break;
				}
			}
			
			// Falls kein gleiches feld gefunden, neues Feld im Scope speichern
			if (notEqual == true){
				scopeObjects.add(newField);
				scopes.put(scope, scopeObjects);
			}
		}
		
		return notEqual; // FEHLER falls true: newField wurde zu scopeObjects und dieses zu scopes hinzugefügt, trotzdem tritt eine Exception auf
	}
}