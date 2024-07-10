package io.quarkus.ts.sqldb.panacheflyway.dbpool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class MySqlDatabaseTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String QUARKUS_DB_JDBC_URL = "quarkus.datasource%s.jdbc.url";
    private static final String QUARKUS_DB_USER = "quarkus.datasource%s.username";
    private static final String QUARKUS_DB_PASSWORD = "quarkus.datasource%s.password";
    private static final String DEFAULT_SCHEMA = "test";
    private static final String MYSQL = "mysql";
    private static final String WITH_XA_PROFILE = ".with-xa";
    private static final String USER_PROPERTY = "MYSQL_USER";
    private static final String PASSWORD_PROPERTY = "MYSQL_PASSWORD";
    private static final String PASSWORD_ROOT_PROPERTY = "MYSQL_ROOT_PASSWORD";
    private static final String DATABASE_PROPERTY = "MYSQL_DATABASE";
    private static final String USER = "user";

    private GenericContainer<?> container;

    @Override
    public Map<String, String> start() {
        String image = System.getProperty("mysql.80.image");

        // we used managed version of 'org.testcontainers:mysql' and version '1.19.7' failed in FIPS-enabled environment
        // over 'SA/ECB/OAEPWithSHA-1AndMGF1Padding' cipher as SunJCE provider was not available in FIPS
        // hence for now, we use GenericContainer which allows to override waiting strategy that works in FIPS
        container = new GenericContainer<>(
                DockerImageName.parse(image).asCompatibleSubstituteFor(MYSQL));
        container.waitingFor(new LogMessageWaitStrategy().withRegEx(".*Only MySQL server logs after this point.*\\s"));
        container.withExposedPorts(3306);
        container.withEnv(USER_PROPERTY, USER);
        container.withEnv(PASSWORD_PROPERTY, USER);
        container.withEnv(PASSWORD_ROOT_PROPERTY, USER);
        container.withEnv(DATABASE_PROPERTY, DEFAULT_SCHEMA);
        container.start();

        var jdbcUrl = "jdbc:mysql://%s:%d/%s".formatted(container.getHost(), container.getMappedPort(3306), DEFAULT_SCHEMA);
        Map<String, String> config = new HashMap<>();
        config.put(defaultDataSource(QUARKUS_DB_JDBC_URL), jdbcUrl);
        config.put(defaultDataSource(QUARKUS_DB_USER), USER);
        config.put(defaultDataSource(QUARKUS_DB_PASSWORD), USER);
        config.put(withXaDataSource(QUARKUS_DB_JDBC_URL), jdbcUrl);
        config.put(withXaDataSource(QUARKUS_DB_USER), USER);
        config.put(withXaDataSource(QUARKUS_DB_PASSWORD), USER);

        return Collections.unmodifiableMap(config);
    }

    @Override
    public void stop() {
        Optional.ofNullable(container).ifPresent(GenericContainer::stop);
    }

    private String defaultDataSource(String key) {
        return String.format(key, StringUtils.EMPTY);
    }

    private String withXaDataSource(String key) {
        return String.format(key, WITH_XA_PROFILE);
    }
}
