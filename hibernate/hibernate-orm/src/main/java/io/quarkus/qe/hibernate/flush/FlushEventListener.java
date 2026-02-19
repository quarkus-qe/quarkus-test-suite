package io.quarkus.qe.hibernate.flush;

import org.hibernate.SessionEventListener;

public class FlushEventListener implements SessionEventListener {

    private int flushCount = 0;
    private int totalEntitiesFlushed = 0;

    public record FlushEventData(
            int flushCount,
            int totalEntitiesFlushed) {
    }

    @Override
    public synchronized void flushEnd(int numberOfEntities, int numberOfCollections) {
        flushCount++;
        totalEntitiesFlushed += numberOfEntities;
    }

    public synchronized FlushEventData getFlushEventData() {
        return new FlushEventData(flushCount, totalEntitiesFlushed);
    }

    public synchronized void resetCounters() {
        flushCount = 0;
        totalEntitiesFlushed = 0;
    }

}
