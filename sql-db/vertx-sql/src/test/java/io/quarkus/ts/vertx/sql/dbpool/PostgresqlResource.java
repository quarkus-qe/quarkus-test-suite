package io.quarkus.ts.vertx.sql.dbpool;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresqlResource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> postgresContainer;
    private static final String POSTGRESQL_IMAGE_NAME = System.getProperty("postgresql.latest.image");

    @Override
    public Map<String, String> start() {
        postgresContainer = new GenericContainer<>(DockerImageName.parse(POSTGRESQL_IMAGE_NAME))
                // Need to set POSTGRES and POSTGRESQL for usage with Docker hub and RH images
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withEnv("POSTGRES_DB", "amadeus")
                .withEnv("POSTGRESQL_USER", "test")
                .withEnv("POSTGRESQL_PASSWORD", "test")
                .withEnv("POSTGRESQL_DATABASE", "amadeus")
                .withExposedPorts(5432);

        postgresContainer.waitingFor(new HostPortWaitStrategy()).start();

        Map<String, String> config = new HashMap<>();
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
