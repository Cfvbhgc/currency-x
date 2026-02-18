package com.currencyx.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class RateHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fromCurrency;
    private String toCurrency;
    private List<CurrencyRate> historicalRates;

    public RateHistory() {
        this.historicalRates = new ArrayList<>();
    }

    public RateHistory(String fromCurrency, String toCurrency, List<CurrencyRate> historicalRates) {
        this.fromCurrency = fromCurrency.toUpperCase();
        this.toCurrency = toCurrency.toUpperCase();
        this.historicalRates = historicalRates != null ? new ArrayList<>(historicalRates) : new ArrayList<>();
    }

    public void appendRateEntry(CurrencyRate rate) {
        this.historicalRates.add(rate);
    }

    public List<CurrencyRate> retrieveUnmodifiableRateList() {
        return Collections.unmodifiableList(historicalRates);
    }

    public int getTotalEntries() {
        return historicalRates.size();
    }

    @Override
    public String toString() {
        return "RateHistory{" +
                "pair=" + fromCurrency + "/" + toCurrency +
                ", entries=" + historicalRates.size() +
                '}';
    }
}
