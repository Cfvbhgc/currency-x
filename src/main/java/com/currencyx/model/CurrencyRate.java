package com.currencyx.model;

import com.currencyx.config.AppConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class CurrencyRate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private Instant timestamp;

    public CurrencyRate() {
    }

    public CurrencyRate(String fromCurrency, String toCurrency, BigDecimal rate, Instant timestamp) {
        this.fromCurrency = fromCurrency.toUpperCase();
        this.toCurrency = toCurrency.toUpperCase();
        this.rate = rate.setScale(AppConstants.RATE_SCALE, AppConstants.DEFAULT_ROUNDING);
        this.timestamp = timestamp;
    }

    public String buildCacheKey() {
        return fromCurrency + AppConstants.RATE_KEY_SEPARATOR + toCurrency;
    }

    @Override
    public String toString() {
        return "CurrencyRate{" +
                "from='" + fromCurrency + '\'' +
                ", to='" + toCurrency + '\'' +
                ", rate=" + rate +
                ", timestamp=" + timestamp +
                '}';
    }
}
