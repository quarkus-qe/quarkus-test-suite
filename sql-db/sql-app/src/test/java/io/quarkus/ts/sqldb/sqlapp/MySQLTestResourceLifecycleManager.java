package io.quarkus.ts.sqldb.sqlapp;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.logging.Log;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class MySQLTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private static final Logger LOGGER = Logger.getLogger(MySQLTestResourceLifecycleManager.class);

    private static final String MYSQL_IMAGE = System.getProperty("mysql.80.image");

    private MySQLContainer<?> mysql;

    @Override
    public Map<String, String> start() {
        LOGGER.info("Starting database container");
        DockerImageName mysqlImage = DockerImageName.parse(MYSQL_IMAGE)
                .asCompatibleSubstituteFor("mysql");
        mysql = new MySQLContainer<>(mysqlImage);
        mysql.start();
        await().pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            LOGGER.info("Waiting for database to start");
            mysql.isRunning();
        });
        Log.warn("jdbc url: " + mysql.getJdbcUrl());
        return Map.of(
                "properties.db.url", mysql.getJdbcUrl(),
                "properties.db.user", mysql.getUsername(),
                "properties.db.password", mysql.getPassword());
    }

    @Override
    public void stop() {
        if (mysql.isRunning()) {
            mysql.stop();
        }
    }
}
