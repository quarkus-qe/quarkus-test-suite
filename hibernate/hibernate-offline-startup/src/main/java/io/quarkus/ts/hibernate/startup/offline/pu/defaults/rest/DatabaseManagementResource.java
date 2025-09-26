package io.quarkus.ts.hibernate.startup.offline.pu.defaults.rest;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.relational.SchemaManager;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.ts.hibernate.startup.offline.pu.defaults.orm.CustomCredentialsProvider;

@Path("/default-pu/database-management/{tenant}/")
public class DatabaseManagementResource {

    public record DatabaseCredentials(String username, String password) {
    }

    @Inject
    SchemaManager schemaManager;

    @Inject
    CustomCredentialsProvider customCredentialsProvider;

    @ConfigProperty(name = "fixed-default-schema")
    Optional<String> fixedDefaultSchema;

    @Consumes(MediaType.APPLICATION_JSON)
    @Path("store-app-scoped-credentials")
    @POST
    public void storeApplicationScopedCredentials(@RestPath String tenant, DatabaseCredentials credentials) {
        customCredentialsProvider.storeCredentials(tenant, credentials.username, credentials.password);
    }

    @Path("create-schema")
    @POST
    public void createSchema(@RestPath String tenant) {
        String schema = fixedDefaultSchema.orElse(tenant);
        schemaManager.forSchema(schema).create(true);
    }

}
