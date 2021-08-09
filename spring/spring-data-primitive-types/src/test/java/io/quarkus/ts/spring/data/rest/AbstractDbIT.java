package io.quarkus.ts.spring.data.rest;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public class AbstractDbIT {
    static final String POSTGRESQL_USER = "user";
    static final String POSTGRESQL_PASSWORD = "user";
    static final String POSTGRESQL_DATABASE = "mydb";
    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "registry.access.redhat.com/rhscl/postgresql-10-rhel7", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static final DefaultService database = new DefaultService()
            .withProperty("POSTGRESQL_USER", POSTGRESQL_USER)
            .withProperty("POSTGRESQL_PASSWORD", POSTGRESQL_PASSWORD)
            .withProperty("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", POSTGRESQL_USER)
            .withProperty("quarkus.datasource.password", POSTGRESQL_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> database.getHost().replace("http", "jdbc:postgresql") + ":" + database.getPort() + "/"
                            + POSTGRESQL_DATABASE);

}
