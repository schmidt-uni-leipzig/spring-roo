package org.springframwork.roo.addon.json;

import flexjson.transformer.AbstractTransformer;

public class NullSerializationTransformer extends AbstractTransformer {

	NullSerialization serializeNull = NullSerialization.REMOVE;
	
	public NullSerialization getSerializeNull() {
		return serializeNull;
	}

	public void setSerializeNull(NullSerialization serializeNull) {
		this.serializeNull = serializeNull;
	}

    public void transform(Object object) 
    {

    	if(this.serializeNull == NullSerialization.REMOVE)
    	{
    		getContext().write("");
    	}
    	else if(this.serializeNull == NullSerialization.DEFAULT)
    	{
    		//TODO
    		//object.getClass().get
            //getContext().writeQuoted((String) object);
    	}
    	else if(this.serializeNull == NullSerialization.EMPTY)
    	{
    		getContext().writeQuoted("");
    	}
    	else if(this.serializeNull == NullSerialization.IGNORE)
    	{
    		getContext().write(object.toString());
    	}
    	else if(this.serializeNull == NullSerialization.NULL)
    	{
    		getContext().write("null");
    	}
    }
}
