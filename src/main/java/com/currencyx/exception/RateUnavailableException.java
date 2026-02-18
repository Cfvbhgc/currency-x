package com.currencyx.exception;

public class RateUnavailableException extends CurrencyServiceException {

    private final String fromCurrency;
    private final String toCurrency;

    public RateUnavailableException(String fromCurrency, String toCurrency) {
        super(String.format("Exchange rate temporarily unavailable for: %s/%s", fromCurrency, toCurrency));
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    public RateUnavailableException(String fromCurrency, String toCurrency, Throwable cause) {
        super(String.format("Exchange rate temporarily unavailable for: %s/%s", fromCurrency, toCurrency), cause);
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }
}
