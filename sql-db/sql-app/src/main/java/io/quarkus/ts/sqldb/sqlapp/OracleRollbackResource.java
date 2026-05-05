package io.quarkus.ts.sqldb.sqlapp;

import java.sql.Connection;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.sql.DataSource;

@Path("/oracle-rollback")
public class OracleRollbackResource {

    private static final Logger LOG = Logger.getLogger(OracleRollbackResource.class);
    private static final String CREATE_TABLE = "CREATE TABLE rollback_test ("
            + "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
            + "value VARCHAR2(255) NOT NULL)";
    private static final String DROP_TABLE = "DROP TABLE rollback_test";

    @Inject
    DataSource dataSource;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    OracleRollbackService oracleRollbackService;

    @POST
    @Path("/init")
    public Response init() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try {
                conn.createStatement().execute(DROP_TABLE);
            } catch (Exception ignored) {
                // table may not exist yet
            }
            conn.createStatement().execute(CREATE_TABLE);
        }
        return Response.ok("initialized").build();
    }

    @POST
    @Path("/trigger")
    public Response trigger() {
        managedExecutor.execute(() -> {
            try {
                oracleRollbackService.insertAndBlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.warn("Transaction failed", e);
            }
        });
        return Response.accepted().build();
    }
}
