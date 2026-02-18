package com.currencyx.service.impl;

import com.currencyx.config.AppConstants;
import com.currencyx.exception.CurrencyNotFoundException;
import com.currencyx.exception.InvalidAmountException;
import com.currencyx.exception.RateUnavailableException;
import com.currencyx.model.ConversionResult;
import com.currencyx.model.CurrencyRate;
import com.currencyx.service.ExchangeRateService;
import com.currencyx.service.MockCentralBankClient;
import com.currencyx.service.RateHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServiceImpl.class);

    private final MockCentralBankClient centralBankClient;
    private final RateHistoryService rateHistoryService;
    private final Map<String, CurrencyRate> latestRatesInMemoryCache;

    ExchangeRateServiceImpl(MockCentralBankClient centralBankClient,
                            RateHistoryService rateHistoryService) {
        this.centralBankClient = centralBankClient;
        this.rateHistoryService = rateHistoryService;
        this.latestRatesInMemoryCache = new ConcurrentHashMap<>();
        initializeExchangeRatesOnStartup();
    }

    @Override
    @Cacheable(value = AppConstants.CACHE_NAME_RATES,
            key = "#fromCurrency.toUpperCase() + '_' + #toCurrency.toUpperCase()")
    public CurrencyRate fetchLatestExchangeRateForPair(String fromCurrency, String toCurrency) {
        validateCurrencyCodeIsSupported(fromCurrency);
        validateCurrencyCodeIsSupported(toCurrency);

        String cacheKey = buildRateCacheKey(fromCurrency, toCurrency);
        CurrencyRate cachedRate = latestRatesInMemoryCache.get(cacheKey);

        if (cachedRate != null) {
            logger.debug("Returning in-memory cached rate for {}/{}", fromCurrency, toCurrency);
            return cachedRate;
        }

        try {
            CurrencyRate freshRate = centralBankClient.fetchSingleRateFromCentralBank(
                    fromCurrency.toUpperCase(), toCurrency.toUpperCase());
            latestRatesInMemoryCache.put(cacheKey, freshRate);
            rateHistoryService.recordExchangeRateSnapshot(freshRate);
            return freshRate;
        } catch (Exception ex) {
            logger.error("Failed to fetch rate for {}/{}", fromCurrency, toCurrency, ex);
            throw new RateUnavailableException(fromCurrency, toCurrency, ex);
        }
    }

    @Override
    public List<CurrencyRate> fetchAllAvailableExchangeRates() {
        if (latestRatesInMemoryCache.isEmpty()) {
            refreshExchangeRatesFromCentralBank();
        }
        return List.copyOf(latestRatesInMemoryCache.values());
    }

    @Override
    public ConversionResult performCurrencyConversion(String fromCurrency, String toCurrency, BigDecimal amount) {
        validateConversionAmountIsWithinBounds(amount);
        validateCurrencyCodeIsSupported(fromCurrency);
        validateCurrencyCodeIsSupported(toCurrency);

        CurrencyRate currentRate = fetchLatestExchangeRateForPair(fromCurrency, toCurrency);
        BigDecimal convertedAmount = amount.multiply(currentRate.getRate(), AppConstants.MATH_CONTEXT)
                .setScale(AppConstants.AMOUNT_SCALE, AppConstants.DEFAULT_ROUNDING);

        logger.info("Converted {} {} to {} {} (rate: {})",
                amount, fromCurrency, convertedAmount, toCurrency, currentRate.getRate());

        return new ConversionResult(
                amount,
                fromCurrency.toUpperCase(),
                toCurrency.toUpperCase(),
                convertedAmount,
                currentRate.getRate(),
                Instant.now()
        );
    }

    @Override
    @CacheEvict(value = AppConstants.CACHE_NAME_RATES, allEntries = true)
    @Scheduled(fixedRateString = "${currency.cache.rate-ttl-seconds:300}000")
    public void refreshExchangeRatesFromCentralBank() {
        logger.info("Refreshing exchange rates from Central Bank...");
        try {
            List<CurrencyRate> freshRates = centralBankClient.fetchLatestExchangeRatesFromCentralBank();
            latestRatesInMemoryCache.clear();

            for (CurrencyRate rate : freshRates) {
                String key = buildRateCacheKey(rate.getFromCurrency(), rate.getToCurrency());
                latestRatesInMemoryCache.put(key, rate);
                rateHistoryService.recordExchangeRateSnapshot(rate);
            }

            logger.info("Successfully refreshed {} exchange rates", freshRates.size());
        } catch (Exception ex) {
            logger.error("Failed to refresh exchange rates from Central Bank", ex);
        }
    }

    private void initializeExchangeRatesOnStartup() {
        logger.info("Initializing exchange rates on application startup");
        try {
            List<CurrencyRate> initialRates = centralBankClient.fetchLatestExchangeRatesFromCentralBank();
            for (CurrencyRate rate : initialRates) {
                String key = buildRateCacheKey(rate.getFromCurrency(), rate.getToCurrency());
                latestRatesInMemoryCache.put(key, rate);
            }
            logger.info("Loaded {} initial exchange rates", initialRates.size());
        } catch (Exception ex) {
            logger.warn("Could not load initial rates, will retry on first request", ex);
        }
    }

    private String buildRateCacheKey(String fromCurrency, String toCurrency) {
        return fromCurrency.toUpperCase() + AppConstants.RATE_KEY_SEPARATOR + toCurrency.toUpperCase();
    }

    private void validateCurrencyCodeIsSupported(String currencyCode) {
        if (currencyCode == null || !AppConstants.SUPPORTED_CURRENCIES.contains(currencyCode.toUpperCase())) {
            throw new CurrencyNotFoundException(currencyCode);
        }
    }

    private void validateConversionAmountIsWithinBounds(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount cannot be null");
        }
        if (amount.compareTo(AppConstants.MINIMUM_CONVERSION_AMOUNT) < 0) {
            throw new InvalidAmountException(amount);
        }
        if (amount.compareTo(AppConstants.MAXIMUM_CONVERSION_AMOUNT) > 0) {
            throw new InvalidAmountException(amount);
        }
    }
}
