package com.currencyx.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ConvertResponse {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private BigDecimal rate;
    private String timestamp;

    public ConvertResponse() {
    }

    public ConvertResponse(String fromCurrency,
                           String toCurrency,
                           BigDecimal originalAmount,
                           BigDecimal convertedAmount,
                           BigDecimal rate,
                           String timestamp) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.originalAmount = originalAmount;
        this.convertedAmount = convertedAmount;
        this.rate = rate;
        this.timestamp = timestamp;
    }
}
