package io.quarkus.ts.security.keycloak.webapp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.InjectableInstance;

@Path("/auth-completion")
public class AuthenticationCompletionResource {

    private static final String COUNT = "SELECT COUNT(*) FROM auth_completion_log";
    private static final String LAST_PRINCIPAL = "SELECT principal_name FROM auth_completion_log ORDER BY id DESC LIMIT 1";
    private static final String TRUNCATE = "TRUNCATE TABLE auth_completion_log";

    @Inject
    AuthenticationCompletionCounter action;

    @Inject
    Instance<AuthenticationCompletionSecondary> secondaryAction;

    @Inject
    InjectableInstance<AgroalDataSource> dataSourceInstance;

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public String count() throws SQLException {
        if (!isDataSourceActive()) {
            return "0";
        }
        try (Connection conn = dataSourceInstance.get().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(COUNT)) {
            return rs.next() ? Integer.toString(rs.getInt(1)) : "0";
        }
    }

    @GET
    @Path("/principal")
    @Produces(MediaType.TEXT_PLAIN)
    public String principal() throws SQLException {
        if (!isDataSourceActive()) {
            return "null";
        }
        try (Connection conn = dataSourceInstance.get().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(LAST_PRINCIPAL)) {
            return rs.next() ? rs.getString(1) : "null";
        }
    }

    @GET
    @Path("/secondary-count")
    @Produces(MediaType.TEXT_PLAIN)
    public String secondaryCount() {
        return secondaryAction.isResolvable() ? Integer.toString(secondaryAction.get().getCallCount()) : "0";
    }

    @POST
    @Path("/enable-fail")
    @Produces(MediaType.TEXT_PLAIN)
    public String enableFail() {
        action.enableFailure();
        return "enabled";
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.TEXT_PLAIN)
    public String reset() throws SQLException {
        action.resetFailure();
        if (secondaryAction.isResolvable()) {
            secondaryAction.get().reset();
        }
        if (isDataSourceActive()) {
            try (Connection conn = dataSourceInstance.get().getConnection();
                    Statement stmt = conn.createStatement()) {
                stmt.execute(TRUNCATE);
            }
        }
        return "reset";
    }

    private boolean isDataSourceActive() {
        return dataSourceInstance.getHandle().getBean().isActive();
    }
}
