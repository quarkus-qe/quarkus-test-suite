package io.quarkus.ts.leak.metrics;

/**
 * Represents class loading deltas between two checkpoints.
 */
public record LeakResult(long loadedDelta, long unloadedDelta) {

    public double unloadRatio() {
        if (loadedDelta == 0) {
            throw new IllegalStateException("No class loading activity detected during measurement.");
        }
        return (double) unloadedDelta / loadedDelta;
    }
}
