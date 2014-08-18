package org.springframework.roo.addon.json.customizing;

public class NullSerializationException extends Exception {
    public NullSerializationException ()
    {
    }

    public NullSerializationException (String message)
    {
    super (message);
    }

    public NullSerializationException (Throwable cause)
    {
    super (cause);
    }

    public NullSerializationException (String message, Throwable cause)
    {
    super (message, cause);
    }
}
