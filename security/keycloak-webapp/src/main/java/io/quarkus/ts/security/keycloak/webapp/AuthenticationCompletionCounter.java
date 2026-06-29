package io.quarkus.ts.security.keycloak.webapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.InjectableInstance;
import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.AuthenticationCompletionAction;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@ApplicationScoped
@Unremovable
public class AuthenticationCompletionCounter implements AuthenticationCompletionAction {

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS auth_completion_log "
            + "(id SERIAL PRIMARY KEY, principal_name VARCHAR(255) NOT NULL)";
    private static final String INSERT = "INSERT INTO auth_completion_log (principal_name) VALUES (?)";

    @Inject
    InjectableInstance<AgroalDataSource> dataSourceInstance;

    private volatile boolean shouldFail;

    void onStart(@Observes StartupEvent event) {
        if (isDataSourceActive()) {
            try (Connection conn = dataSourceInstance.get().getConnection();
                    Statement stmt = conn.createStatement()) {
                stmt.execute(CREATE_TABLE);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Uni<Void> action(AuthenticationCompletionContext authCompletionContext) {
        if (shouldFail) {
            return Uni.createFrom().failure(new RuntimeException("Forced authentication completion failure"));
        }
        if (isDataSourceActive()) {
            String principal = authCompletionContext.identity().getPrincipal().getName();
            return Uni.createFrom().voidItem()
                    .emitOn(Infrastructure.getDefaultWorkerPool())
                    .invoke(() -> {
                        try (Connection conn = dataSourceInstance.get().getConnection();
                                PreparedStatement ps = conn.prepareStatement(INSERT)) {
                            ps.setString(1, principal);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return Uni.createFrom().voidItem();
    }

    private boolean isDataSourceActive() {
        return dataSourceInstance.getHandle().getBean().isActive();
    }

    void enableFailure() {
        shouldFail = true;
    }

    void resetFailure() {
        shouldFail = false;
    }
}
