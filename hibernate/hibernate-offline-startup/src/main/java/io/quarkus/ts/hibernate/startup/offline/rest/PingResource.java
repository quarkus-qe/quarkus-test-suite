package io.quarkus.ts.hibernate.startup.offline.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("ping")
public class PingResource {

    @ConfigProperty(name = "quarkus.hibernate-orm.database.start-offline")
    boolean startOffline;

    @Inject
    EntityManager entityManager;

    @Path("start-offline")
    @GET
    public String startOffline() {
        return "pong: " + startOffline;
    }

    @Path("database/{tenant}")
    @GET
    public String database() {
        return "pong: " + databaseOnline();
    }

    private boolean databaseOnline() {
        return entityManager.createQuery("select 1").getSingleResult().equals(1);
    }

}
