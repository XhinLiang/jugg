package com.xhinliang.jugg.exception;

/**
 * @author xhinliang
 */
public class JuggRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JuggRuntimeException(String message) {
        super(message);
    }

    public JuggRuntimeException(Throwable cause) {
        super(cause);
    }
}
