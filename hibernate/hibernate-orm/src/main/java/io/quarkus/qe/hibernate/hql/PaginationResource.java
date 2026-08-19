package io.quarkus.qe.hibernate.hql;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.Session;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("/pagination")
public class PaginationResource {

    @PersistenceUnit("test-hql-pu")
    @Inject
    Session session;

    @GET
    @Path("/orders-with-items")
    @Produces(MediaType.TEXT_PLAIN)
    public String getOrdersWithItems(
            @QueryParam("firstResult") int firstResult,
            @QueryParam("maxResults") int maxResults) {
        List<Orders> orders = session.createQuery(
                "SELECT DISTINCT o FROM Orders o JOIN FETCH o.items ORDER BY o.id",
                Orders.class)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
        return orders.stream()
                .map(o -> o.customerName + ":" + o.items.size())
                .collect(Collectors.joining(","));
    }
}
