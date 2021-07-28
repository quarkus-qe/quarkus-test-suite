package io.quarkus.ts.memoryLeaks;

import io.vertx.core.json.JsonObject;

/**
 * MemoryReport represents a summary of the memory usage per a given long running test
 */
public class MemoryReport {

    private int totalOfBuckets;
    private long minMemoryUsage;
    private long minAvgMemoryUsage;
    private long maxMemoryUsage;
    private long maxAvgMemoryUsage;
    private long afterGcMinMemoryUsage;
    private long afterGcMinAvgMemoryUsage;
    private long afterGcMaxMemoryUsage;
    private long afterGcMaxAvgMemoryUsage;
    private int minBucketsDeviation;
    private int maxBucketsDeviation;
    private int threshold;
    private boolean minLeaks;
    private boolean maxLeaks;
    private boolean memoryLeaks;

    public MemoryReport(MemoryBucket memoryBucket, MemoryBucket afterGarbageCollectorInvoked, int threshold) {

        this.threshold = threshold;
        this.totalOfBuckets = memoryBucket.size();
        this.maxMemoryUsage = memoryBucket.getOverAllMax();
        this.maxAvgMemoryUsage = memoryBucket.getMaxAvg();
        this.minMemoryUsage = memoryBucket.getOverAllMin();
        this.minAvgMemoryUsage = memoryBucket.getMinAvg();

        this.afterGcMinMemoryUsage = afterGarbageCollectorInvoked.getOverAllMin();
        this.afterGcMinAvgMemoryUsage = afterGarbageCollectorInvoked.getMinAvg();
        this.afterGcMaxMemoryUsage = afterGarbageCollectorInvoked.getOverAllMax();
        this.afterGcMaxAvgMemoryUsage = afterGarbageCollectorInvoked.getMaxAvg();

        this.minBucketsDeviation = memoryBucket.minOverAllDeviation(afterGarbageCollectorInvoked);
        this.maxBucketsDeviation = memoryBucket.maxOverAllDeviation(afterGarbageCollectorInvoked);

        boolean isAppMinGreater = memoryBucket.getMinAvg() < afterGarbageCollectorInvoked.getOverAllMin();
        boolean isAppMaxGreater = memoryBucket.getMaxAvg() < afterGarbageCollectorInvoked.getOverAllMax();
        this.minLeaks = minBucketsDeviation > threshold && isAppMinGreater;
        this.maxLeaks = maxBucketsDeviation > threshold && isAppMaxGreater;
        this.memoryLeaks = minLeaks;
    }

    public int getTotalOfBuckets() {
        return totalOfBuckets;
    }

    public void setTotalOfBuckets(int totalOfBuckets) {
        this.totalOfBuckets = totalOfBuckets;
    }

    public long getMinMemoryUsage() {
        return minMemoryUsage;
    }

    public void setMinMemoryUsage(long minMemoryUsage) {
        this.minMemoryUsage = minMemoryUsage;
    }

    public long getMaxMemoryUsage() {
        return maxMemoryUsage;
    }

    public void setMaxMemoryUsage(long maxMemoryUsage) {
        this.maxMemoryUsage = maxMemoryUsage;
    }

    public int getMinBucketsDeviation() {
        return minBucketsDeviation;
    }

    public void setMinBucketsDeviation(int minBucketsDeviation) {
        this.minBucketsDeviation = minBucketsDeviation;
    }

    public int getMaxBucketsDeviation() {
        return maxBucketsDeviation;
    }

    public void setMaxBucketsDeviation(int maxBucketsDeviation) {
        this.maxBucketsDeviation = maxBucketsDeviation;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isMinLeaks() {
        return minLeaks;
    }

    public void setMinLeaks(boolean minLeaks) {
        this.minLeaks = minLeaks;
    }

    public boolean isMaxLeaks() {
        return maxLeaks;
    }

    public void setMaxLeaks(boolean maxLeaks) {
        this.maxLeaks = maxLeaks;
    }

    public boolean isMemoryLeaks() {
        return memoryLeaks;
    }

    public void setMemoryLeaks(boolean memoryLeaks) {
        this.memoryLeaks = memoryLeaks;
    }

    public long getAfterGcMinMemoryUsage() {
        return afterGcMinMemoryUsage;
    }

    public void setAfterGcMinMemoryUsage(long afterGcMinMemoryUsage) {
        this.afterGcMinMemoryUsage = afterGcMinMemoryUsage;
    }

    public long getAfterGcMaxMemoryUsage() {
        return afterGcMaxMemoryUsage;
    }

    public void setAfterGcMaxMemoryUsage(long afterGcMaxMemoryUsage) {
        this.afterGcMaxMemoryUsage = afterGcMaxMemoryUsage;
    }

    public String reportPretty() {
        return JsonObject.mapFrom(this).encodePrettily();
    }

    public void setMinAvgMemoryUsage(long minAvgMemoryUsage) {
        this.minAvgMemoryUsage = minAvgMemoryUsage;
    }

    public void setMaxAvgMemoryUsage(long maxAvgMemoryUsage) {
        this.maxAvgMemoryUsage = maxAvgMemoryUsage;
    }

    public void setAfterGcMinAvgMemoryUsage(long afterGcMinAvgMemoryUsage) {
        this.afterGcMinAvgMemoryUsage = afterGcMinAvgMemoryUsage;
    }

    public void setAfterGcMaxAvgMemoryUsage(long afterGcMaxAvgMemoryUsage) {
        this.afterGcMaxAvgMemoryUsage = afterGcMaxAvgMemoryUsage;
    }

    public long getMinAvgMemoryUsage() {
        return minAvgMemoryUsage;
    }

    public long getMaxAvgMemoryUsage() {
        return maxAvgMemoryUsage;
    }

    public long getAfterGcMinAvgMemoryUsage() {
        return afterGcMinAvgMemoryUsage;
    }

    public long getAfterGcMaxAvgMemoryUsage() {
        return afterGcMaxAvgMemoryUsage;
    }
}
