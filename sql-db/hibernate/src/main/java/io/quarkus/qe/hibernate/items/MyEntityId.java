package io.quarkus.qe.hibernate.items;

import jakarta.persistence.Embeddable;

@Embeddable
public class MyEntityId {

    private long entityId;

    protected MyEntityId() {
    }

    public MyEntityId(long entityId) {
        this.entityId = entityId;
    }

    public long getVal() {
        return entityId;
    }

    public void setVal(long entityId) {
        this.entityId = entityId;
    }
}
