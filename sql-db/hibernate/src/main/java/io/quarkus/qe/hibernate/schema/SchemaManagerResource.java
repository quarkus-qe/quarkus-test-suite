package io.quarkus.qe.hibernate.schema;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.SchemaManager;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/schema")
public class SchemaManagerResource {

    @Named("named")
    @Inject
    SchemaManager schemaManager;

    @DELETE
    @Path("/drop")
    public void dropSchema() {
        schemaManager.drop(true);
    }

    @POST
    @Path("/create")
    public void createSchema() {
        schemaManager.create(true);
    }
}
