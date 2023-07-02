package io.quarkus.ts.sqldb.multiplepus;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.ts.sqldb.multiplepus.model.fungus.Fungus;

@Path("fungus")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FungusResource {

    @GET
    public long countAll() {
        return Fungus.count();
    }

}
