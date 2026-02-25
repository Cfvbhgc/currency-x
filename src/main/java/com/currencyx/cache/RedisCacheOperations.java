package com.currencyx.cache;

import com.currencyx.config.AppConstants;
import com.currencyx.model.CurrencyRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheOperations {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheOperations.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheOperations(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeExchangeRateInCache(String key, CurrencyRate rate, long ttlSeconds) {
        try {
            String fullKey = AppConstants.REDIS_RATE_KEY_PREFIX + key;
            redisTemplate.opsForValue().set(fullKey, rate, ttlSeconds, TimeUnit.SECONDS);
            logger.debug("Stored rate in Redis: {} -> {}", fullKey, rate.getRate());
        } catch (Exception ex) {
            logger.warn("Failed to store rate in Redis cache for key: {}", key, ex);
        }
    }

    public CurrencyRate retrieveExchangeRateFromCache(String key) {
        try {
            String fullKey = AppConstants.REDIS_RATE_KEY_PREFIX + key;
            Object cached = redisTemplate.opsForValue().get(fullKey);
            if (cached instanceof CurrencyRate) {
                logger.debug("Cache hit for rate key: {}", fullKey);
                return (CurrencyRate) cached;
            }
        } catch (Exception ex) {
            logger.warn("Failed to retrieve rate from Redis cache for key: {}", key, ex);
        }
        return null;
    }

    public void appendToRateHistoryList(String historyKey, CurrencyRate rate) {
        try {
            redisTemplate.opsForList().rightPush(historyKey, rate);
            Long currentSize = redisTemplate.opsForList().size(historyKey);
            if (currentSize != null && currentSize > AppConstants.MAX_HISTORY_LIMIT) {
                redisTemplate.opsForList().trim(historyKey,
                        currentSize - AppConstants.MAX_HISTORY_LIMIT, -1);
            }
            redisTemplate.expire(historyKey, AppConstants.DEFAULT_HISTORY_TTL_SECONDS * 24, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.warn("Failed to append to rate history in Redis for key: {}", historyKey, ex);
        }
    }

    public List<CurrencyRate> retrieveRateHistoryEntries(String historyKey, int limit) {
        try {
            Long totalSize = redisTemplate.opsForList().size(historyKey);
            if (totalSize == null || totalSize == 0) {
                return Collections.emptyList();
            }

            long startIndex = Math.max(0, totalSize - limit);
            List<Object> rawEntries = redisTemplate.opsForList().range(historyKey, startIndex, -1);

            if (rawEntries == null) {
                return Collections.emptyList();
            }

            List<CurrencyRate> rates = new ArrayList<>();
            for (Object entry : rawEntries) {
                if (entry instanceof CurrencyRate) {
                    rates.add((CurrencyRate) entry);
                }
            }
            return rates;
        } catch (Exception ex) {
            logger.warn("Failed to retrieve rate history from Redis for key: {}", historyKey, ex);
            return Collections.emptyList();
        }
    }

    public void evictCacheEntryByKey(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            logger.debug("Evicted cache entry: {} (result: {})", key, deleted);
        } catch (Exception ex) {
            logger.warn("Failed to evict cache entry: {}", key, ex);
        }
    }

    public boolean checkIfKeyExistsInCache(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception ex) {
            logger.warn("Failed to check key existence in Redis: {}", key, ex);
            return false;
        }
    }
}
