package com.currencyx.config;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Set;

public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    public static final String CACHE_NAME_RATES = "exchange-rates";
    public static final String CACHE_NAME_HISTORY = "rate-history";

    public static final long DEFAULT_RATE_TTL_SECONDS = 300L;
    public static final long DEFAULT_HISTORY_TTL_SECONDS = 3600L;

    public static final int RATE_SCALE = 6;
    public static final int AMOUNT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    public static final MathContext MATH_CONTEXT = new MathContext(16, DEFAULT_ROUNDING);

    public static final int DEFAULT_HISTORY_LIMIT = 30;
    public static final int MAX_HISTORY_LIMIT = 365;

    public static final BigDecimal MINIMUM_CONVERSION_AMOUNT = BigDecimal.valueOf(0.01);
    public static final BigDecimal MAXIMUM_CONVERSION_AMOUNT = new BigDecimal("999999999.99");

    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_GBP = "GBP";
    public static final String CURRENCY_JPY = "JPY";
    public static final String CURRENCY_CNY = "CNY";
    public static final String CURRENCY_RUB = "RUB";
    public static final String CURRENCY_CHF = "CHF";
    public static final String CURRENCY_CAD = "CAD";
    public static final String CURRENCY_AUD = "AUD";
    public static final String CURRENCY_BRL = "BRL";

    public static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            CURRENCY_USD, CURRENCY_EUR, CURRENCY_GBP, CURRENCY_JPY,
            CURRENCY_CNY, CURRENCY_RUB, CURRENCY_CHF, CURRENCY_CAD,
            CURRENCY_AUD, CURRENCY_BRL
    );

    public static final String RATE_KEY_SEPARATOR = "_";
    public static final String REDIS_KEY_PREFIX = "currencyx:";
    public static final String REDIS_RATE_KEY_PREFIX = REDIS_KEY_PREFIX + "rate:";
    public static final String REDIS_HISTORY_KEY_PREFIX = REDIS_KEY_PREFIX + "history:";

    public static final String ERROR_CURRENCY_NOT_FOUND = "Currency pair not found: %s/%s";
    public static final String ERROR_RATE_UNAVAILABLE = "Exchange rate temporarily unavailable for: %s/%s";
    public static final String ERROR_INVALID_AMOUNT = "Invalid conversion amount: %s";
    public static final String ERROR_UNSUPPORTED_CURRENCY = "Unsupported currency code: %s";
}
