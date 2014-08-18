package org.springframework.roo.addon.json.customizing;

public class ShallowSerializationObjectException extends Exception {
    public ShallowSerializationObjectException ()
    {
    }

    public ShallowSerializationObjectException (String message)
    {
    super (message);
    }

    public ShallowSerializationObjectException (Throwable cause)
    {
    super (cause);
    }

    public ShallowSerializationObjectException (String message, Throwable cause)
    {
    super (message, cause);
    }
}
