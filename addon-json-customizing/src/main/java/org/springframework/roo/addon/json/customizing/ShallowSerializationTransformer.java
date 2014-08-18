package org.springframework.roo.addon.json.customizing;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import flexjson.JSONSerializer;
import flexjson.TransformerUtil;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;

public class ShallowSerializationTransformer extends AbstractTransformer{
	
	private String path;
	private boolean isSet = false;
	
	public ShallowSerializationTransformer(String path)
	{
		this.path = path.endsWith("/") ? path : path + "/";
	}

    public void transform(Object object) 
    {
    	if(object == null)
    	{
    		getContext().write("null");
    		return;
    	}
    	
    	//TODO check if set always applies
    	if(object instanceof java.util.Set)
    	{
    		isSet = true;
    		getContext().writeOpenArray();
    		Object[] set = ((java.util.Set<Object>) object).toArray();
    		for(int i=0; i< set.length; i++)
    		{
    			if(i > 0)
    				getContext().writeComma();
    			writeShallowSerializationObject(set[i]);
    		}
    		getContext().writeCloseArray();
    	}
    	else
    		writeShallowSerializationObject(object);
    }

	private void writeShallowSerializationObject(Object object) {
		String id = object.toString();

    	if(!id.contains("id="))  //not!
    	{
    		Transformer trans = TransformerUtil.getDefaultTypeTransformers().get(object.getClass());
    		trans.transform(object);
    	}
    	else
    	{
    		//workaround for not having the trouble of including hibernat.PersistentSet !!
    		int startPos = id.indexOf("id=") + 3;
    		int endPos = id.substring(startPos).indexOf(',');
    		if(endPos < 0)
    			endPos = id.substring(startPos).indexOf(']');
    		id = id.subSequence(startPos, startPos+endPos).toString();
	    	try {
				getContext().write((new ShallowSerializationObject(object, path + id)).toJson());
			} catch (ShallowSerializationObjectException e) {
				getContext().writeQuoted(e.getMessage());
			}
    	}
	}
}
