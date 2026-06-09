package io.quarkus.qe.hibernate.spatial.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.qe.hibernate.spatial.entity.jts.JtsPlace;
import io.quarkus.qe.hibernate.spatial.entity.jts.JtsRoute;

@ApplicationScoped
public class JtsSpatialDatasetService {

    @PersistenceUnit("spatial")
    @Inject
    EntityManager em;

    @Transactional
    public void init() {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        em.persist(new JtsPlace(factory.createPoint(new Coordinate(1, 1))));
        em.persist(new JtsPlace(factory.createPoint(new Coordinate(5, 5))));
        em.persist(new JtsPlace(factory.createPoint(new Coordinate(9, 9))));
        em.persist(new JtsPlace(factory.createPoint(new Coordinate(20, 20))));

        LineString line = factory.createLineString(new Coordinate[] {
                new Coordinate(-5, 5),
                new Coordinate(15, 5)
        });
        em.persist(new JtsRoute(line));

        em.flush();
    }
}
