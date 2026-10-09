package io.quarkus.ts.packaging.semeruscc;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Exercises Hibernate ORM + H2, which causes significant class-loading work that the SCC must cache correctly.
 * Any extension-specific startup breakage under SCC will surface here as a ClassNotFoundException at runtime.
 */
@Path("/fruits")
public class FruitResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Fruit> all() {
        return entityManager.createQuery("from Fruit order by name", Fruit.class).getResultList();
    }
}
