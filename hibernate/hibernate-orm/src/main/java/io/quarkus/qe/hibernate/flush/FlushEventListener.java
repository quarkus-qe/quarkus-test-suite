package io.quarkus.qe.hibernate.flush;

import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.SessionEventListener;

public class FlushEventListener implements SessionEventListener {

    private final AtomicInteger flushCount = new AtomicInteger(0);
    private final AtomicInteger totalEntitiesFlushed = new AtomicInteger(0);

    public record FlushEventData(
            int flushCount,
            int totalEntitiesFlushed) {
    }

    @Override
    public void flushEnd(int numberOfEntities, int numberOfCollections) {
        flushCount.incrementAndGet();
        totalEntitiesFlushed.addAndGet(numberOfEntities);
    }

    public FlushEventData getFlushEventData() {
        return new FlushEventData(
                flushCount.get(),
                totalEntitiesFlushed.get());
    }

    public void resetCounters() {
        flushCount.set(0);
        totalEntitiesFlushed.set(0);
    }

}
