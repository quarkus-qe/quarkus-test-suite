package org.acme;

import io.quarkus.hibernate.panache.Panache;
import io.quarkus.hibernate.panache.PanacheEntity;
import io.quarkus.hibernate.panache.PanacheEntityMarker;
import io.quarkus.hibernate.panache.PanacheQuery;
import io.quarkus.hibernate.panache.PanacheRepository;
import io.quarkus.hibernate.panache.blocking.PanacheBlockingQuery;

public class MyPanacheEntity extends PanacheEntity {

    public String name;

    public interface Repository extends PanacheRepository<Object> {
    }

    private PanacheQuery<Object, Object, Object, Object> query;
    private PanacheEntityMarker marker;
    private PanacheBlockingQuery<Object> blockingQuery;

    public class MyPanache extends Panache {
        public String name;
    }
}
