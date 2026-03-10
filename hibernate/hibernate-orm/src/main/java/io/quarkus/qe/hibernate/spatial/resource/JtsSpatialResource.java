package io.quarkus.qe.hibernate.spatial.resource;

import jakarta.ws.rs.Path;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import io.quarkus.qe.hibernate.spatial.entity.jts.JtsPlace;
import io.quarkus.qe.hibernate.spatial.entity.jts.JtsRegion;
import io.quarkus.qe.hibernate.spatial.entity.jts.JtsRoute;

@Path("/spatial/jts")
public class JtsSpatialResource extends AbstractSpatialResource {

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    protected String placeEntity() {
        return JtsPlace.class.getSimpleName();
    }

    @Override
    protected String regionEntity() {
        return JtsRegion.class.getSimpleName();
    }

    @Override
    protected String routeEntity() {
        return JtsRoute.class.getSimpleName();
    }

    @Override
    protected Object referencePoint() {
        return factory.createPoint(new Coordinate(5, 5));
    }

    @Override
    protected Object referencePointAfterMove(double x, double y) {
        return factory.createPoint(new Coordinate(x, y));
    }

    @Override
    protected void updateLocation(Object entity, double x, double y) {
        JtsPlace place = (JtsPlace) entity;
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        place.setLocation(factory.createPoint(new Coordinate(x, y)));
    }
}
