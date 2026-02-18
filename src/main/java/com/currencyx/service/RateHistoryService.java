package com.currencyx.service;

import com.currencyx.cache.RedisCacheOperations;
import com.currencyx.config.AppConstants;
import com.currencyx.exception.CurrencyNotFoundException;
import com.currencyx.model.CurrencyRate;
import com.currencyx.model.RateHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(RateHistoryService.class);

    private final RedisCacheOperations redisCacheOperations;

    RateHistoryService(RedisCacheOperations redisCacheOperations) {
        this.redisCacheOperations = redisCacheOperations;
    }

    public void recordExchangeRateSnapshot(CurrencyRate rate) {
        String historyKey = buildHistoryKeyForCurrencyPair(rate.getFromCurrency(), rate.getToCurrency());
        redisCacheOperations.appendToRateHistoryList(historyKey, rate);
        logger.debug("Recorded rate snapshot for {}/{}: {}",
                rate.getFromCurrency(), rate.getToCurrency(), rate.getRate());
    }

    @Cacheable(value = AppConstants.CACHE_NAME_HISTORY,
            key = "#fromCurrency + '_' + #toCurrency + '_' + #limit")
    public RateHistory retrieveRateHistoryForCurrencyPair(String fromCurrency, String toCurrency, int limit) {
        validateCurrencyCodesAreSupported(fromCurrency, toCurrency);
        int effectiveLimit = Math.min(Math.max(limit, 1), AppConstants.MAX_HISTORY_LIMIT);

        String historyKey = buildHistoryKeyForCurrencyPair(fromCurrency, toCurrency);
        List<CurrencyRate> historicalRates = redisCacheOperations
                .retrieveRateHistoryEntries(historyKey, effectiveLimit);

        logger.info("Retrieved {} historical rates for {}/{}",
                historicalRates.size(), fromCurrency, toCurrency);

        return new RateHistory(fromCurrency, toCurrency, historicalRates);
    }

    @CacheEvict(value = AppConstants.CACHE_NAME_HISTORY, allEntries = true)
    public void clearAllHistoryCacheEntries() {
        logger.info("Cleared all rate history cache entries");
    }

    private String buildHistoryKeyForCurrencyPair(String fromCurrency, String toCurrency) {
        return AppConstants.REDIS_HISTORY_KEY_PREFIX
                + fromCurrency.toUpperCase()
                + AppConstants.RATE_KEY_SEPARATOR
                + toCurrency.toUpperCase();
    }

    private void validateCurrencyCodesAreSupported(String fromCurrency, String toCurrency) {
        if (!AppConstants.SUPPORTED_CURRENCIES.contains(fromCurrency.toUpperCase())) {
            throw new CurrencyNotFoundException(fromCurrency);
        }
        if (!AppConstants.SUPPORTED_CURRENCIES.contains(toCurrency.toUpperCase())) {
            throw new CurrencyNotFoundException(toCurrency);
        }
    }
}
