package com.currencyx.service;

import com.currencyx.config.AppConstants;
import com.currencyx.model.CurrencyRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
class MockCentralBankClient {

    private static final Logger logger = LoggerFactory.getLogger(MockCentralBankClient.class);

    private final Map<String, BigDecimal> baseRatesAgainstUsd;
    private final double fluctuationPercent;

    MockCentralBankClient(@Value("${currency.mock-bank.fluctuation-percent:0.5}") double fluctuationPercent) {
        this.fluctuationPercent = fluctuationPercent;
        this.baseRatesAgainstUsd = initializeBaseRatesAgainstUsd();
    }

    private Map<String, BigDecimal> initializeBaseRatesAgainstUsd() {
        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put(AppConstants.CURRENCY_USD, BigDecimal.ONE);
        rates.put(AppConstants.CURRENCY_EUR, new BigDecimal("0.9234"));
        rates.put(AppConstants.CURRENCY_GBP, new BigDecimal("0.7891"));
        rates.put(AppConstants.CURRENCY_JPY, new BigDecimal("149.8700"));
        rates.put(AppConstants.CURRENCY_CNY, new BigDecimal("7.2456"));
        rates.put(AppConstants.CURRENCY_RUB, new BigDecimal("92.4530"));
        rates.put(AppConstants.CURRENCY_CHF, new BigDecimal("0.8812"));
        rates.put(AppConstants.CURRENCY_CAD, new BigDecimal("1.3567"));
        rates.put(AppConstants.CURRENCY_AUD, new BigDecimal("1.5342"));
        rates.put(AppConstants.CURRENCY_BRL, new BigDecimal("4.9723"));
        return rates;
    }

    List<CurrencyRate> fetchLatestExchangeRatesFromCentralBank() {
        logger.debug("Fetching simulated exchange rates from mock Central Bank API");
        List<CurrencyRate> fetchedRates = new ArrayList<>();
        Instant now = Instant.now();

        List<String> currencies = new ArrayList<>(AppConstants.SUPPORTED_CURRENCIES);
        for (int i = 0; i < currencies.size(); i++) {
            for (int j = 0; j < currencies.size(); j++) {
                if (i == j) {
                    continue;
                }
                String from = currencies.get(i);
                String to = currencies.get(j);
                BigDecimal rate = calculateCrossRateWithFluctuation(from, to);
                fetchedRates.add(new CurrencyRate(from, to, rate, now));
            }
        }

        logger.info("Fetched {} exchange rates from mock Central Bank", fetchedRates.size());
        return fetchedRates;
    }

    CurrencyRate fetchSingleRateFromCentralBank(String fromCurrency, String toCurrency) {
        BigDecimal rate = calculateCrossRateWithFluctuation(fromCurrency, toCurrency);
        return new CurrencyRate(fromCurrency, toCurrency, rate, Instant.now());
    }

    private BigDecimal calculateCrossRateWithFluctuation(String fromCurrency, String toCurrency) {
        BigDecimal fromToUsd = baseRatesAgainstUsd.get(fromCurrency);
        BigDecimal toToUsd = baseRatesAgainstUsd.get(toCurrency);

        if (fromToUsd == null || toToUsd == null) {
            throw new IllegalArgumentException("Unknown currency in rate calculation");
        }

        BigDecimal crossRate = toToUsd.divide(fromToUsd, AppConstants.MATH_CONTEXT);
        BigDecimal fluctuation = generateRealisticFluctuation();

        return crossRate.multiply(fluctuation, AppConstants.MATH_CONTEXT)
                .setScale(AppConstants.RATE_SCALE, AppConstants.DEFAULT_ROUNDING);
    }

    private BigDecimal generateRealisticFluctuation() {
        double randomFactor = 1.0 + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * (fluctuationPercent / 100.0);
        return BigDecimal.valueOf(randomFactor);
    }
}
