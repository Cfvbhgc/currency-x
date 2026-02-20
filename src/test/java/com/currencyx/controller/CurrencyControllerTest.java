package com.currencyx.controller;

import com.currencyx.model.ConversionResult;
import com.currencyx.model.CurrencyRate;
import com.currencyx.model.RateHistory;
import com.currencyx.service.ExchangeRateService;
import com.currencyx.service.RateHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private RateHistoryService rateHistoryService;

    @Test
    @DisplayName("GET /api/rates should return all available rates")
    void shouldReturnAllExchangeRatesSuccessfully() throws Exception {
        CurrencyRate usdEur = new CurrencyRate("USD", "EUR",
                new BigDecimal("0.923400"), Instant.now());
        CurrencyRate usdGbp = new CurrencyRate("USD", "GBP",
                new BigDecimal("0.789100"), Instant.now());

        when(exchangeRateService.fetchAllAvailableExchangeRates())
                .thenReturn(List.of(usdEur, usdGbp));

        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fromCurrency").value("USD"))
                .andExpect(jsonPath("$[0].toCurrency").value("EUR"));
    }

    @Test
    @DisplayName("GET /api/rates/{from}/{to} should return specific rate")
    void shouldReturnSpecificExchangeRateForPair() throws Exception {
        CurrencyRate rate = new CurrencyRate("USD", "EUR",
                new BigDecimal("0.923400"), Instant.now());

        when(exchangeRateService.fetchLatestExchangeRateForPair("USD", "EUR"))
                .thenReturn(rate);

        mockMvc.perform(get("/api/rates/USD/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.923400));
    }

    @Test
    @DisplayName("POST /api/convert should perform conversion")
    void shouldPerformCurrencyConversionViaRestEndpoint() throws Exception {
        ConversionResult result = new ConversionResult(
                new BigDecimal("100.00"), "USD", "EUR",
                new BigDecimal("92.34"), new BigDecimal("0.923400"),
                Instant.now());

        when(exchangeRateService.performCurrencyConversion(eq("USD"), eq("EUR"), any(BigDecimal.class)))
                .thenReturn(result);

        String requestBody = """
                {
                    "fromCurrency": "USD",
                    "toCurrency": "EUR",
                    "amount": 100.00
                }
                """;

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"))
                .andExpect(jsonPath("$.convertedAmount").value(92.34));
    }

    @Test
    @DisplayName("POST /api/convert should reject invalid request body")
    void shouldRejectConversionRequestWithMissingFields() throws Exception {
        String invalidBody = """
                {
                    "fromCurrency": "USD"
                }
                """;

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/history/{from}/{to} should return rate history")
    void shouldReturnRateHistoryForCurrencyPair() throws Exception {
        CurrencyRate historicalRate = new CurrencyRate("USD", "EUR",
                new BigDecimal("0.920000"), Instant.now().minusSeconds(3600));
        RateHistory history = new RateHistory("USD", "EUR", List.of(historicalRate));

        when(rateHistoryService.retrieveRateHistoryForCurrencyPair(eq("USD"), eq("EUR"), anyInt()))
                .thenReturn(history);

        mockMvc.perform(get("/api/history/USD/EUR")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("EUR"))
                .andExpect(jsonPath("$.historicalRates.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/convert should reject negative amount")
    void shouldRejectConversionWithNegativeAmount() throws Exception {
        String requestBody = """
                {
                    "fromCurrency": "USD",
                    "toCurrency": "EUR",
                    "amount": -50.00
                }
                """;

        mockMvc.perform(post("/api/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
