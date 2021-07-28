package io.quarkus.ts.memoryLeaks;

/**
 * MemoryPairs represents a change in trend, from greater to a minor value.
 */
public class MemoryPairs {
    private long min;
    private long max;

    public MemoryPairs(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
}
