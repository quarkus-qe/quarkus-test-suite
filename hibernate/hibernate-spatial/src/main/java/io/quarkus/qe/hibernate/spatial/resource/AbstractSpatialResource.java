package io.quarkus.qe.hibernate.spatial.resource;

import java.util.List;
import java.util.function.Consumer;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Transactional
public abstract class AbstractSpatialResource {

    @Inject
    @PersistenceUnit("spatial")
    EntityManager em;

    protected abstract String placeEntity();

    protected abstract String regionEntity();

    protected abstract String routeEntity();

    protected abstract Object referencePoint();

    protected abstract void updateLocation(Object entity, double x, double y);

    protected abstract Object referencePointAfterMove(double x, double y);

    protected <T> T singleResult(String jpql, Class<T> type) {
        return em.createQuery(jpql, type)
                .setMaxResults(1)
                .getSingleResult();
    }

    protected List<String> sorted(List<String> list) {
        return list.stream().sorted().toList();
    }

    protected List<String> geometryListQuery(String query, Consumer<TypedQuery<String>> binder) {
        TypedQuery<String> typedQuery = em.createQuery(query, String.class);

        if (binder != null) {
            binder.accept(typedQuery);
        }

        return sorted(typedQuery.getResultList());
    }

    protected Object regionPolygonGeometry() {
        return singleResult(
                "select r.area from " + regionEntity() + " r",
                Object.class);
    }

    @GET
    @Path("/contains")
    public List<String> contains() {
        return geometryListQuery("""
                select ST_AsText(p.location)
                from %s p
                where ST_Contains(:polygon, p.location)
                """.formatted(placeEntity()),
                q -> q.setParameter("polygon", regionPolygonGeometry()));
    }

    @GET
    @Path("/within")
    public List<String> within() {
        return geometryListQuery("""
                select ST_AsText(p.location)
                from %s p
                where ST_Within(p.location, :region)
                """.formatted(placeEntity()),
                q -> q.setParameter("region", regionPolygonGeometry()));
    }

    @GET
    @Path("/distance-order")
    public List<Double> distanceOrder() {
        return em.createQuery("""
                select ST_Distance(p.location, :point)
                from %s p
                order by ST_Distance(p.location, :point)
                """.formatted(placeEntity()), Double.class)
                .setParameter("point", referencePoint())
                .getResultList();
    }

    @GET
    @Path("/route-intersects-region")
    public Boolean intersects() {
        return em.createQuery("""
                select ST_Intersects(r.area, route.path)
                from %s r, %s route
                """.formatted(regionEntity(), routeEntity()), Boolean.class)
                .setMaxResults(1)
                .getSingleResult();
    }

    @GET
    @Path("/route")
    public String getRouteLine() {
        return singleResult("""
                select ST_AsText(r.path)
                from %s r
                """.formatted(routeEntity()), String.class);
    }

    @GET
    @Path("/reference-point/geometry")
    public String getReferencePoint() {
        return em.createQuery("""
                select ST_AsText(p.location)
                from %s p
                where ST_Equals(p.location, :point)
                """.formatted(placeEntity()), String.class)
                .setParameter("point", referencePoint())
                .getSingleResult();
    }

    @POST
    @Path("/reference-point/move")
    @Transactional
    public String moveReferencePoint(@QueryParam("x") double x, @QueryParam("y") double y) {
        Object place = em.createQuery("""
                select p
                from %s p
                where ST_Distance(p.location, :point) = 0
                """.formatted(placeEntity()), Object.class)
                .setParameter("point", referencePoint())
                .getSingleResult();

        updateLocation(place, x, y);
        em.flush();

        return em.createQuery("""
                select ST_AsText(p.location)
                from %s p
                where ST_Distance(p.location, :newPoint) = 0
                """.formatted(placeEntity()), String.class)
                .setParameter("newPoint", referencePointAfterMove(x, y))
                .setMaxResults(1)
                .getSingleResult();
    }
}
