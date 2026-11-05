package io.quarkus.ts.vertx.sql.dbpool;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresqlResource implements QuarkusTestResourceLifecycleManager {

    // SCRAM-SHA-256 (Postgres) uses the password as an HMAC key; FIPS PKCS#11 (NSS) rejects short keys (CKR_KEY_SIZE_RANGE), so the short passwords fail
    // TODO: set the FIPS-safe (long) password in the Postgres DatabaseService of the test framework and drop this workaround
    private static final String POSTGRESQL_PASSWORD = "quarkus_test_fips_long_password";
    private GenericContainer<?> postgresContainer;
    private static final String POSTGRESQL_IMAGE_NAME = System.getProperty("postgresql.latest.image");

    @Override
    public Map<String, String> start() {
        // on Aarch64 we use PostgreSQL from Red Hat container registry
        // this image is difficult to run with `org.testcontainers.containers.PostgreSQLContainer`
        // see comments in the https://github.com/quarkusio/quarkus/issues/44196 for more info
        postgresContainer = new GenericContainer<>(DockerImageName.parse(POSTGRESQL_IMAGE_NAME))
                // Need to set POSTGRES and POSTGRESQL for usage with Docker hub and RH images
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", POSTGRESQL_PASSWORD)
                .withEnv("POSTGRES_DB", "amadeus")
                .withEnv("POSTGRESQL_USER", "test")
                .withEnv("POSTGRESQL_PASSWORD", POSTGRESQL_PASSWORD)
                .withEnv("POSTGRESQL_DATABASE", "amadeus")
                .withExposedPorts(5432);

        postgresContainer.waitingFor(new HostPortWaitStrategy()).start();

        Map<String, String> config = new HashMap<>();
        // main application.properties hard-codes the datasource password
        config.put("quarkus.datasource.password", POSTGRESQL_PASSWORD);
        config.put("quarkus.datasource.jdbc.url", String.format("jdbc:postgresql://%s:%d/amadeus", postgresContainer.getHost(),
                postgresContainer.getFirstMappedPort()));
        config.put("quarkus.datasource.reactive.url", String.format("postgresql://%s:%d/amadeus", postgresContainer.getHost(),
                postgresContainer.getFirstMappedPort()));
        config.put("app.selected.db", "postgresql");
        // Enable Flyway for Postgresql
        config.put("quarkus.flyway.migrate-at-start", "true");
        // Disable Flyway for MySQL
        config.put("quarkus.flyway.mysql.migrate-at-start", "false");
        // Disable Flyway for MSSQL
        config.put("quarkus.flyway.mssql.migrate-at-start", "false");

        return config;
    }

    @Override
    public void stop() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }
}
