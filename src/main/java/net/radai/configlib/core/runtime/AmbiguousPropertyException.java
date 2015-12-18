package net.radai.configlib.core.runtime;

/**
 * @author Radai Rosenblatt
 */
public class AmbiguousPropertyException extends IllegalArgumentException {
    public AmbiguousPropertyException() {
    }

    public AmbiguousPropertyException(String s) {
        super(s);
    }

    public AmbiguousPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousPropertyException(Throwable cause) {
        super(cause);
    }
}
