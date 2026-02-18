package com.currencyx.exception;

public class CurrencyNotFoundException extends CurrencyServiceException {

    private final String fromCurrency;
    private final String toCurrency;

    public CurrencyNotFoundException(String fromCurrency, String toCurrency) {
        super(String.format("Currency pair not found: %s/%s", fromCurrency, toCurrency));
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    public CurrencyNotFoundException(String currencyCode) {
        super(String.format("Unsupported currency code: %s", currencyCode));
        this.fromCurrency = currencyCode;
        this.toCurrency = null;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }
}
