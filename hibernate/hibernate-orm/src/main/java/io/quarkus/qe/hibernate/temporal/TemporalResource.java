package io.quarkus.qe.hibernate.temporal;

import java.time.Instant;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.SessionFactory;

import io.quarkus.hibernate.orm.PersistenceUnit;
import io.quarkus.runtime.StartupEvent;

@Path("/temporal")
public class TemporalResource {

    @PersistenceUnit("temporal-pu")
    @Inject
    SessionFactory sessionFactory;

    private volatile Instant snapshotInstant;

    public void startup(@Observes StartupEvent event) throws InterruptedException {
        sessionFactory.inTransaction(session -> {
            session.persist(new TemporalProduct(1L, "Original Product"));
        });

        Thread.sleep(500);
        snapshotInstant = Instant.now();
        Thread.sleep(500);

        sessionFactory.inTransaction(session -> {
            var product = session.find(TemporalProduct.class, 1L);
            product.setName("Updated Product");
        });
    }

    @GET
    @Path("/current/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCurrentProduct(@PathParam("id") long id) {
        return sessionFactory.fromSession(session -> {
            var product = session.find(TemporalProduct.class, id);
            return product != null ? product.getName() : "not found";
        });
    }

    @GET
    @Path("/historical/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getHistoricalProduct(@PathParam("id") long id) {
        try (var session = sessionFactory.withOptions().asOf(snapshotInstant).open()) {
            var product = session.find(TemporalProduct.class, id);
            return product != null ? product.getName() : "not found";
        }
    }
}
