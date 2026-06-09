package io.quarkus.qe.hibernate.spatial.resource;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

import jakarta.ws.rs.Path;

import io.quarkus.qe.hibernate.spatial.entity.geolatte.GeolattePlace;
import io.quarkus.qe.hibernate.spatial.entity.geolatte.GeolatteRegion;
import io.quarkus.qe.hibernate.spatial.entity.geolatte.GeolatteRoute;

@Path("/spatial/geolatte")
public class GeolatteSpatialResource extends AbstractSpatialResource {

    @Override
    protected String placeEntity() {
        return GeolattePlace.class.getSimpleName();
    }

    @Override
    protected String regionEntity() {
        return GeolatteRegion.class.getSimpleName();
    }

    @Override
    protected String routeEntity() {
        return GeolatteRoute.class.getSimpleName();
    }

    @Override
    protected Object referencePoint() {
        return point(WGS84, g(5, 5));
    }

    @Override
    protected Object referencePointAfterMove(double x, double y) {
        return point(WGS84, g(x, y));
    }

    @Override
    protected void updateLocation(Object entity, double x, double y) {
        GeolattePlace place = (GeolattePlace) entity;
        place.setLocation(point(WGS84, g(x, y)));
    }
}
