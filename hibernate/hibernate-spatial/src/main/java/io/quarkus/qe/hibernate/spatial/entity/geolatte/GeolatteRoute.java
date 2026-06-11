package io.quarkus.qe.hibernate.spatial.entity.geolatte;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.geolatte.geom.G2D;
import org.geolatte.geom.LineString;

@Entity
public class GeolatteRoute {

    @Id
    @GeneratedValue
    Long id;

    LineString<G2D> path;

    public GeolatteRoute() {
    }

    public GeolatteRoute(LineString<G2D> path) {
        this.path = path;
    }

    public LineString<G2D> getPath() {
        return path;
    }
}
