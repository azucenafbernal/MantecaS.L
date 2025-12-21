package com.mantecasl.accommodationapp.business.exception;

public class ReservaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReservaException(String message) {
        super(message);
    }

    public ReservaException(String message, Throwable cause) {
        super(message, cause);
    }
}
