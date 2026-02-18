package com.currencyx.service;

import com.currencyx.model.ConversionResult;
import com.currencyx.model.CurrencyRate;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeRateService {

    CurrencyRate fetchLatestExchangeRateForPair(String fromCurrency, String toCurrency);

    List<CurrencyRate> fetchAllAvailableExchangeRates();

    ConversionResult performCurrencyConversion(String fromCurrency, String toCurrency, BigDecimal amount);

    void refreshExchangeRatesFromCentralBank();
}
