package com.currencyx.controller;

import com.currencyx.config.AppConstants;
import com.currencyx.controller.dto.ConvertRequest;
import com.currencyx.controller.dto.ConvertResponse;
import com.currencyx.controller.dto.RateResponse;
import com.currencyx.model.ConversionResult;
import com.currencyx.model.CurrencyRate;
import com.currencyx.model.RateHistory;
import com.currencyx.service.ExchangeRateService;
import com.currencyx.service.RateHistoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
class CurrencyController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

    private final ExchangeRateService exchangeRateService;
    private final RateHistoryService rateHistoryService;

    CurrencyController(ExchangeRateService exchangeRateService,
                       RateHistoryService rateHistoryService) {
        this.exchangeRateService = exchangeRateService;
        this.rateHistoryService = rateHistoryService;
    }

    @GetMapping("/rates")
    ResponseEntity<List<RateResponse>> retrieveAllAvailableExchangeRates() {
        logger.info("REST request: GET /api/rates");
        List<CurrencyRate> rates = exchangeRateService.fetchAllAvailableExchangeRates();

        List<RateResponse> responseList = rates.stream()
                .map(this::mapCurrencyRateToRateResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/rates/{from}/{to}")
    ResponseEntity<RateResponse> retrieveExchangeRateForSpecificPair(
            @PathVariable("from") String fromCurrency,
            @PathVariable("to") String toCurrency) {

        logger.info("REST request: GET /api/rates/{}/{}", fromCurrency, toCurrency);
        CurrencyRate rate = exchangeRateService.fetchLatestExchangeRateForPair(fromCurrency, toCurrency);
        return ResponseEntity.ok(mapCurrencyRateToRateResponse(rate));
    }

    @PostMapping("/convert")
    ResponseEntity<ConvertResponse> performCurrencyConversionViaRest(
            @Valid @RequestBody ConvertRequest request) {

        logger.info("REST request: POST /api/convert - {} {} -> {}",
                request.getAmount(), request.getFromCurrency(), request.getToCurrency());

        ConversionResult result = exchangeRateService.performCurrencyConversion(
                request.getFromCurrency(),
                request.getToCurrency(),
                request.getAmount()
        );

        ConvertResponse response = new ConvertResponse(
                result.getFromCurrency(),
                result.getToCurrency(),
                result.getOriginalAmount(),
                result.getConvertedAmount(),
                result.getAppliedRate(),
                result.getTimestamp().toString()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{from}/{to}")
    ResponseEntity<RateHistory> retrieveExchangeRateHistoryForPair(
            @PathVariable("from") String fromCurrency,
            @PathVariable("to") String toCurrency,
            @RequestParam(value = "limit", defaultValue = "30") int limit) {

        logger.info("REST request: GET /api/history/{}/{} (limit={})", fromCurrency, toCurrency, limit);
        RateHistory history = rateHistoryService.retrieveRateHistoryForCurrencyPair(
                fromCurrency, toCurrency, limit);

        return ResponseEntity.ok(history);
    }

    private RateResponse mapCurrencyRateToRateResponse(CurrencyRate rate) {
        return new RateResponse(
                rate.getFromCurrency(),
                rate.getToCurrency(),
                rate.getRate(),
                rate.getTimestamp().toString()
        );
    }
}
