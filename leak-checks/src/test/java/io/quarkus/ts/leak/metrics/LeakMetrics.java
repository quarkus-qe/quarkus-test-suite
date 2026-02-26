package io.quarkus.ts.leak.metrics;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * Utility for measuring class loading and unloading deltas between two lifecycle checkpoints.
 */
public final class LeakMetrics {

    private final ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();

    private long initialLoaded;
    private long initialUnloaded;

    public void record() {
        initialLoaded = bean.getTotalLoadedClassCount();
        initialUnloaded = bean.getUnloadedClassCount();
    }

    public LeakResult measure() {
        long loaded = bean.getTotalLoadedClassCount() - initialLoaded;
        long unloaded = bean.getUnloadedClassCount() - initialUnloaded;
        return new LeakResult(loaded, unloaded);
    }
}