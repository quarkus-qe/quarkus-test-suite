package io.quarkus.ts.hibernate.startup.offline.pu.ownconnection.rest;

import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.relational.SchemaManager;
import org.jboss.resteasy.reactive.RestPath;

@Path("/own-connection-provider-pu/database-management/{tenant}/")
public class DatabaseManagementResource {

    @Named("own_connection_provider")
    @Inject
    SchemaManager schemaManager;

    @ConfigProperty(name = "fixed-default-schema")
    Optional<String> fixedDefaultSchema;

    @Path("create-schema")
    @POST
    public void createSchema(@RestPath String tenant) {
        String schema = fixedDefaultSchema.orElse(tenant);
        schemaManager.forSchema(schema).create(true);
    }

}
