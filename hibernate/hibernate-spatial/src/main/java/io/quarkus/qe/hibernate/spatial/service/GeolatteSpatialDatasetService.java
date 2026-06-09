package io.quarkus.qe.hibernate.spatial.service;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Polygon;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.qe.hibernate.spatial.entity.geolatte.GeolattePlace;
import io.quarkus.qe.hibernate.spatial.entity.geolatte.GeolatteRegion;

@ApplicationScoped
public class GeolatteSpatialDatasetService {

    @PersistenceUnit("spatial")
    @Inject
    EntityManager em;

    @Transactional
    public void init() {
        Polygon<G2D> polygon = polygon(WGS84, ring(
                g(0, 0),
                g(10, 0),
                g(10, 10),
                g(0, 10),
                g(0, 0)));

        em.persist(new GeolattePlace(point(WGS84, g(1, 1))));
        em.persist(new GeolattePlace(point(WGS84, g(5, 5))));
        em.persist(new GeolattePlace(point(WGS84, g(9, 9))));
        em.persist(new GeolattePlace(point(WGS84, g(20, 20))));
        em.persist(new GeolatteRegion(polygon));

        em.flush();
    }
}
