package io.quarkus.ts.memoryLeaks;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * MemoryBucket is a cluster of memory pairs, each memory pair represent a change of trends in terms of memory usage
 */
public class MemoryBucket {

    private LinkedList<MemoryPairs> storage = new LinkedList<>();

    public void upsert(long memoryUsed) {
        if (isEmpty() || getCurrentMax() > memoryUsed) {
            storage.add(new MemoryPairs(memoryUsed, memoryUsed));
            return;
        }

        MemoryPairs memoryPairs = storage.getLast();
        if (memoryPairs.getMax() < memoryUsed) {
            memoryPairs.setMax(memoryUsed);
        }
    }

    public int size() {
        return storage.size();
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public long getOverAllMax() {
        return storage.stream().mapToLong(MemoryPairs::getMax).max().orElseThrow(NoSuchElementException::new);
    }

    public long getMinAvg() {
        return storage.stream().mapToLong(MemoryPairs::getMin).sum() / storage.size();
    }

    public long getOverAllMin() {
        return storage.stream().mapToLong(MemoryPairs::getMin).min().orElseThrow(NoSuchElementException::new);
    }

    public long getMaxAvg() {
        return storage.stream().mapToLong(MemoryPairs::getMax).sum() / storage.size();
    }

    public int minOverAllDeviation(MemoryBucket other) {
        long afterGCMin = other.getOverAllMin();
        long min = getMinAvg();

        long dif = Math.abs(min - afterGCMin);
        return percentage(min, dif).intValue();
    }

    public int maxOverAllDeviation(MemoryBucket other) {
        long afterGCMax = other.getOverAllMax();
        long max = getOverAllMax();
        long dif = Math.abs(max - afterGCMax);
        return percentage(max, dif).intValue();
    }

    private Number percentage(long whole, long part) {
        return 100 * part / whole;
    }

    private long getCurrentMax() {
        return storage.getLast().getMax();
    }
}
