package io.quarkus.qe.hibernate.items;

import java.io.Serializable;

import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

import com.github.benmanes.caffeine.cache.Weigher;

public class DataSizeWeigher implements Weigher<Object, Object> {
    @Override
    public int weigh(Object key, Object value) {
        if (value instanceof AbstractReadWriteAccess.Item item) {
            value = item.getValue();
        }
        if (value instanceof CacheEntry cacheEntry) {
            int weight = 0;
            for (Serializable state : cacheEntry.getDisassembledState()) {
                if (state instanceof String string) {
                    weight += string.length();
                }
            }
            return Math.max(weight, 1);
        }
        return 1;
    }
}
