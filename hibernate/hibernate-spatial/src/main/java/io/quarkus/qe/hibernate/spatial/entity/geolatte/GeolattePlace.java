package io.quarkus.qe.hibernate.spatial.entity.geolatte;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

@Entity
public class GeolattePlace {

    @Id
    @GeneratedValue
    Long id;

    Point<G2D> location;

    public GeolattePlace() {
    }

    public GeolattePlace(Point<G2D> location) {
        this.location = location;
    }

    public Point<G2D> getLocation() {
        return location;
    }

    public void setLocation(Point<G2D> location) {
        this.location = location;
    }
}
