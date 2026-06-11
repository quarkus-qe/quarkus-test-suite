package io.quarkus.qe.hibernate.spatial.entity.jts;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.locationtech.jts.geom.Polygon;

@Entity
public class JtsRegion {

    @Id
    @GeneratedValue
    Long id;

    Polygon area;

    public JtsRegion() {
    }

    public JtsRegion(Polygon area) {
        this.area = area;
    }

    public Polygon getArea() {
        return area;
    }
}
