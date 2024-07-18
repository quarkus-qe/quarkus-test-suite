package io.quarkus.ts.service;

import java.sql.SQLException;
import java.sql.Statement;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import io.agroal.api.AgroalDataSource;

@Path("/service")
public class ServiceResource {
    @Inject
    AgroalDataSource defaultDataSource;

    // Dummy change
    @Path("/grant/{user}")
    @POST
    public Response grant(String user) throws SQLException {
        try (Statement statement = defaultDataSource.getConnection().createStatement()) {
            Response.Status status = statement.execute("grant XA_RECOVER_ADMIN on *.* to '" + user + "'@'%';")
                    ? Response.Status.CREATED
                    : Response.Status.NO_CONTENT;
            return Response.status(status).build();
        }
    }
}
