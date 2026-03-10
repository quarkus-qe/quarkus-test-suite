package io.quarkus.qe.hibernate.spatial.entity.geolatte;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Polygon;

@Entity
public class GeolatteRegion {

    @Id
    @GeneratedValue
    Long id;

    Polygon<G2D> area;

    public GeolatteRegion() {
    }

    public GeolatteRegion(Polygon<G2D> area) {
        this.area = area;
    }

    public Polygon<G2D> getArea() {
        return area;
    }
}
