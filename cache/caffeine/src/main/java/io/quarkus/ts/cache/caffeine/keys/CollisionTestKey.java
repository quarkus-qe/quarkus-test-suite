package io.quarkus.ts.cache.caffeine.keys;

import java.util.Objects;

public class CollisionTestKey {
    private String id;

    public CollisionTestKey() {
    }

    public CollisionTestKey(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CollisionTestKey collisionTestKey = (CollisionTestKey) obj;
        return Objects.equals(id, collisionTestKey.id);
    }
}
