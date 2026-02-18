package com.currencyx.exception;

public abstract class CurrencyServiceException extends RuntimeException {

    protected CurrencyServiceException(String message) {
        super(message);
    }

    protected CurrencyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
