package com.currencyx.service;

import com.currencyx.exception.CurrencyNotFoundException;
import com.currencyx.exception.InvalidAmountException;
import com.currencyx.model.ConversionResult;
import com.currencyx.model.CurrencyRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ExchangeRateServiceImplTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("Should fetch exchange rate for valid currency pair")
    void shouldFetchExchangeRateForValidCurrencyPair() {
        CurrencyRate rate = exchangeRateService.fetchLatestExchangeRateForPair("USD", "EUR");

        assertNotNull(rate);
        assertEquals("USD", rate.getFromCurrency());
        assertEquals("EUR", rate.getToCurrency());
        assertNotNull(rate.getRate());
        assertTrue(rate.getRate().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(rate.getTimestamp());
    }

    @Test
    @DisplayName("Should return all available exchange rates")
    void shouldReturnAllAvailableExchangeRates() {
        List<CurrencyRate> rates = exchangeRateService.fetchAllAvailableExchangeRates();

        assertNotNull(rates);
        assertFalse(rates.isEmpty());
        assertTrue(rates.size() > 10, "Expected more than 10 rate pairs");
    }

    @Test
    @DisplayName("Should perform currency conversion correctly")
    void shouldPerformCurrencyConversionWithValidParameters() {
        BigDecimal amount = new BigDecimal("100.00");
        ConversionResult result = exchangeRateService.performCurrencyConversion("USD", "EUR", amount);

        assertNotNull(result);
        assertEquals("USD", result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
        assertEquals(0, amount.compareTo(result.getOriginalAmount()));
        assertTrue(result.getConvertedAmount().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(result.getAppliedRate());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("Should throw CurrencyNotFoundException for unsupported currency")
    void shouldThrowExceptionForUnsupportedCurrencyCode() {
        assertThrows(CurrencyNotFoundException.class, () ->
                exchangeRateService.fetchLatestExchangeRateForPair("USD", "XYZ"));
    }

    @Test
    @DisplayName("Should throw InvalidAmountException for negative amount")
    void shouldThrowExceptionForNegativeConversionAmount() {
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        assertThrows(InvalidAmountException.class, () ->
                exchangeRateService.performCurrencyConversion("USD", "EUR", negativeAmount));
    }

    @Test
    @DisplayName("Should throw InvalidAmountException for null amount")
    void shouldThrowExceptionForNullConversionAmount() {
        assertThrows(InvalidAmountException.class, () ->
                exchangeRateService.performCurrencyConversion("USD", "EUR", null));
    }

    @Test
    @DisplayName("Should handle conversion with large amounts correctly")
    void shouldHandleLargeAmountConversion() {
        BigDecimal largeAmount = new BigDecimal("500000.00");
        ConversionResult result = exchangeRateService.performCurrencyConversion("GBP", "JPY", largeAmount);

        assertNotNull(result);
        assertTrue(result.getConvertedAmount().compareTo(largeAmount) > 0,
                "JPY amount should be larger than GBP amount");
    }

    @Test
    @DisplayName("Should use BigDecimal scale for rate precision")
    void shouldMaintainCorrectDecimalScaleForRates() {
        CurrencyRate rate = exchangeRateService.fetchLatestExchangeRateForPair("USD", "JPY");

        assertNotNull(rate);
        assertEquals(6, rate.getRate().scale(), "Rate should have scale of 6");
    }
}
