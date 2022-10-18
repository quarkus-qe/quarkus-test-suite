package io.quarkus.ts.cache.spring;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public abstract class BaseServiceWithCache {

    public static final String DEFAULT_CACHE_VALUE = "reset";
    private static final String CACHE_NAME = "service-cache";

    private static int counter = 0;

    @Cacheable(CACHE_NAME)
    public String getValue() {
        return "Value: " + counter++;
    }

    @CacheEvict(CACHE_NAME)
    public void invalidate() {
        // do nothing
    }

    @Cacheable(CACHE_NAME)
    public String getValueWithPrefix(String prefix) {
        return StringEscapeUtils.escapeHtml4(prefix) + ": " + counter++;
    }

    @CacheEvict(CACHE_NAME)
    public void invalidateWithPrefix(String prefix) {
        // do nothing
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void invalidateAll() {
        // do nothing
    }

    @CachePut(CACHE_NAME)
    public String resetCache() {
        return DEFAULT_CACHE_VALUE;
    }

    @CachePut(CACHE_NAME)
    public String resetCacheWithPrefix(String prefix) {
        return DEFAULT_CACHE_VALUE;
    }
}
