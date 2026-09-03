package io.quarkus.ts.packaging.treeshake;

import java.util.List;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Exercises data sources (Hibernate ORM + H2, JDBC driver loaded via reflection/ServiceLoader)
 * and REST with Jackson serialization.
 */
@Path("/fruits")
public class FruitResource {

    @Inject
    EntityManager entityManager;

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public List<Fruit> all() {
        return entityManager.createQuery("from Fruit order by name", Fruit.class).getResultList();
    }
}
