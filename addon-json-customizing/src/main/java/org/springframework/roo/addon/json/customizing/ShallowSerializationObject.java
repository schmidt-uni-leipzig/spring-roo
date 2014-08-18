package org.springframework.roo.addon.json.customizing;

public class ShallowSerializationObject 
{
	private String href;
	private String rel = "self";
	private String method = "GET";
	
	public ShallowSerializationObject(Object obj, String path) throws ShallowSerializationObjectException
	{
		if(!obj.getClass().isPrimitive()) //not!!
		{
			href = path;
		}
		else
			throw new ShallowSerializationObjectException("primitive types can not be shallowly serialized");
	}
	
	public String toJson()
	{
		return "{ \"href\":\"" + href + "\", \"rel\":\"" + rel + "\", \"method\":\"" + method + "\" }";
	}
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
}
