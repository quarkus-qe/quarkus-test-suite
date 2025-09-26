package io.quarkus.ts.hibernate.startup.offline.test;

import static io.quarkus.test.services.containers.DockerContainerManagedResource.DOCKER_INNER_CONTAINER;

import java.io.IOException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@Tag("fips-incompatible") // MSSQL works with BC JSSE FIPS which is not native-compatible, we test FIPS elsewhere
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@QuarkusScenario
public class SqlServerHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    private static final String JDBC_URL = "jdbc:sqlserver://localhost:1433;databaseName=%s;encrypt=false;";

    @Container(image = "${mssql.image}", port = 1433, expectedLog = "Service Broker manager has started", builder = FixedPortResourceBuilder.class)
    static final SqlServerService db = new SqlServerService().setAutoStart(false).onPostStart(service -> {
        SqlServerService self = (SqlServerService) service;
        String[] commands = {
                "CREATE DATABASE app_scope_credentials_db",
                "CREATE DATABASE req_scope_credentials_db",
                "CREATE DATABASE own_connection_provider_db"
        };
        for (String command : commands) {
            try {
                var result = self
                        .<GenericContainer<?>> getPropertyFromContext(DOCKER_INNER_CONTAINER)
                        .execInContainer(
                                "/opt/mssql-tools18/bin/sqlcmd", "-C", "-S", "localhost", "-U", self.getUser(),
                                "-P", self.getPassword(), "-Q", command);
                Log.debug(self, "Command execution result is: %s", result);
                if (result.getStderr() != null && !result.getStderr().isBlank()) {
                    Log.error("Failed to execute command: " + result.getStderr());
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    });

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mssql"))
    static final RestService app = new RestService()
            .withProperty("quarkus.hibernate-orm.multitenant", "DATABASE")
            .withProperty("fixed-default-schema", "dbo")
            .withProperty("quarkus.datasource.app_scope_credentials.jdbc.url", getJdbcUrl("app_scope_credentials_db"))
            .withProperty("quarkus.datasource.req_scope_credentials.jdbc.url", getJdbcUrl("req_scope_credentials_db"))
            .withProperty("quarkus.datasource.own_connection_provider.jdbc.url", getJdbcUrl("own_connection_provider_db"));

    private static String getJdbcUrl(String dbName) {
        return JDBC_URL.formatted(dbName);
    }
}
