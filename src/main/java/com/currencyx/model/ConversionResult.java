package com.currencyx.model;

import com.currencyx.config.AppConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class ConversionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal originalAmount;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal convertedAmount;
    private BigDecimal appliedRate;
    private Instant timestamp;

    public ConversionResult() {
    }

    public ConversionResult(BigDecimal originalAmount,
                            String fromCurrency,
                            String toCurrency,
                            BigDecimal convertedAmount,
                            BigDecimal appliedRate,
                            Instant timestamp) {
        this.originalAmount = originalAmount.setScale(AppConstants.AMOUNT_SCALE, AppConstants.DEFAULT_ROUNDING);
        this.fromCurrency = fromCurrency.toUpperCase();
        this.toCurrency = toCurrency.toUpperCase();
        this.convertedAmount = convertedAmount.setScale(AppConstants.AMOUNT_SCALE, AppConstants.DEFAULT_ROUNDING);
        this.appliedRate = appliedRate.setScale(AppConstants.RATE_SCALE, AppConstants.DEFAULT_ROUNDING);
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "amount=" + originalAmount +
                " " + fromCurrency +
                " -> " + convertedAmount +
                " " + toCurrency +
                ", rate=" + appliedRate +
                '}';
    }
}
