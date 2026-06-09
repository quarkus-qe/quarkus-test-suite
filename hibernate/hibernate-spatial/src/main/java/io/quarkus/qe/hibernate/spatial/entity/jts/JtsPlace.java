package io.quarkus.qe.hibernate.spatial.entity.jts;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.locationtech.jts.geom.Point;

@Entity
public class JtsPlace {

    @Id
    @GeneratedValue
    Long id;

    Point location;

    public JtsPlace() {
    }

    public JtsPlace(Point location) {
        this.location = location;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
