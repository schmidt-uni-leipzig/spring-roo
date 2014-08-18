package org.springframework.roo.addon.json.customizing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface  NullSerializationAnnotation 
{
	public NullSerialization nullSerialization() default NullSerialization.REMOVE;
	 
	public String defaultValue() default "";
}
