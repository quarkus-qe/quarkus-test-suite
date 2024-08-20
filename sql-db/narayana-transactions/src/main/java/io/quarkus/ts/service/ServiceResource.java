package io.quarkus.ts.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;

@Path("/service")
public class ServiceResource {
    private static final Logger LOG = Logger.getLogger(ServiceResource.class);

    @Inject
    AgroalDataSource defaultDataSource;

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

    @Path("/connections")
    @GET
    public Response connections() {
        try (Statement statement = defaultDataSource.getConnection().createStatement()) {
            ResultSet set = statement.executeQuery("SELECT COUNT(*) from sys.dm_exec_connections;");
            if (set.next()) {
                int count = set.getInt(1);
                return Response.ok(count).build();
            }
            return Response.serverError().build();
        } catch (Exception e) {
            LOG.error("Failed to retrieve number of connections", e);
            return Response.serverError().build();
        }
    }
}
