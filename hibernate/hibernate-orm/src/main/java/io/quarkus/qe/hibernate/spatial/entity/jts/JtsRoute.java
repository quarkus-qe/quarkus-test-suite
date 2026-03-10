package io.quarkus.qe.hibernate.spatial.entity.jts;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.locationtech.jts.geom.LineString;

@Entity
public class JtsRoute {

    @Id
    @GeneratedValue
    Long id;

    LineString path;

    public JtsRoute() {
    }

    public JtsRoute(LineString path) {
        this.path = path;
    }

    public LineString getPath() {
        return path;
    }
}