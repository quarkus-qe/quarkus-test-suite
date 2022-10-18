package io.quarkus.ts.cache.spring;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RestControllerWithCacheResource {

    private static final String CACHE_NAME = "api-cache";

    private static int counter = 0;

    @GetMapping
    @Cacheable(CACHE_NAME)
    public String getValue() {
        return "Value: " + counter++;
    }

    @PostMapping("/invalidate-cache")
    @CacheEvict(CACHE_NAME)
    public void invalidate() {
        // do nothing
    }

    @PostMapping("/cache/reset")
    @CachePut(CACHE_NAME)
    public String resetValueInCache() {
        return BaseServiceWithCache.DEFAULT_CACHE_VALUE;
    }

    @GetMapping("/using-prefix/{prefix}")
    @Cacheable(CACHE_NAME)
    public String getValueWithPrefix(@PathVariable("prefix") String prefix) {
        return StringEscapeUtils.escapeHtml4(prefix) + ": " + counter++;
    }

    @PostMapping("/using-prefix/{prefix}/invalidate-cache")
    @CacheEvict(CACHE_NAME)
    public void invalidateWithPrefix(@PathVariable("prefix") String prefix) {
        // do nothing
    }

    @PostMapping("/using-prefix/{prefix}/cache/reset")
    @CachePut(CACHE_NAME)
    public String resetValueInCacheWithPrefix(@PathVariable("prefix") String prefix) {
        return BaseServiceWithCache.DEFAULT_CACHE_VALUE;
    }

    @PostMapping("/invalidate-cache-all")
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void invalidateAll() {
        // do nothing
    }
}
