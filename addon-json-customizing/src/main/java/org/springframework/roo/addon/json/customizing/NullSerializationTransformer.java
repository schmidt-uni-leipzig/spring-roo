package org.springframework.roo.addon.json.customizing;

import java.util.LinkedList;

import flexjson.JSONContext;
import flexjson.JSONSerializer;
import flexjson.OutputHandler;
import flexjson.StringBuilderOutputHandler;
import flexjson.TransformerUtil;
import flexjson.transformer.*;

public class NullSerializationTransformer extends AbstractTransformer {

	private NullSerialization serializeNull = NullSerialization.NULL;
	private String defaultValue;
	
	public NullSerializationTransformer(NullSerialization serializeNull) throws NullSerializationException
	{
		if(serializeNull == NullSerialization.DEFAULT)
			throw new NullSerializationException("NullSerialization.DEFAULT needs a default value");
		this.serializeNull = serializeNull;
	}
	
	public NullSerializationTransformer(NullSerialization serializeNull, String defaultValue) throws NullSerializationException
	{		
		if(serializeNull == NullSerialization.DEFAULT){
			this.serializeNull = serializeNull;
			this.defaultValue = defaultValue;
		}
		else
			throw new NullSerializationException("a default value needs NullSerialization.DEFAULT");
	}

    public void transform(Object object) 
    {

    	if (object == null) 
    	{
			if (this.serializeNull == NullSerialization.DEFAULT) {
				getContext().write(defaultValue);
			} else if (this.serializeNull == NullSerialization.EMPTY) {
				getContext().writeQuoted("");
			} else if (this.serializeNull == NullSerialization.NULL) {
				getContext().write("null");
			}
			else if (this.serializeNull == NullSerialization.REMOVE) 
			{
				OutputHandler h = new StringBuilderOutputHandler(
						new StringBuilder());
				String current = getContext().getOut().toString();
				h.write(current, 0, current.lastIndexOf(','));
				getContext().setOut(h);
			}
		}
    	else
    	{
    		Transformer trans = TransformerUtil.getDefaultTypeTransformers().get(object.getClass());
    		trans.transform(object);
    	}
    		
    }
}
