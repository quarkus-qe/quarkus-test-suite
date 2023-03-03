package io.quarkus.ts.scheduling.quartz.basic.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CounterService {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public void reset(String caller) {
        counters.putIfAbsent(caller, new AtomicInteger());
    }

    public int get(String caller) {
        var count = counters.get(caller);
        return count == null ? -1 : count.get();
    }

    public void invoke(String caller) {
        counters.get(caller).incrementAndGet();
    }
}
