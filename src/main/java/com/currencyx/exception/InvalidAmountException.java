package com.currencyx.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends CurrencyServiceException {

    private final BigDecimal amount;

    public InvalidAmountException(BigDecimal amount) {
        super(String.format("Invalid conversion amount: %s", amount));
        this.amount = amount;
    }

    public InvalidAmountException(String message) {
        super(message);
        this.amount = null;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
