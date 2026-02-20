package com.currencyx.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RateResponse {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private String timestamp;

    public RateResponse() {
    }

    public RateResponse(String fromCurrency, String toCurrency, BigDecimal rate, String timestamp) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.timestamp = timestamp;
    }
}
