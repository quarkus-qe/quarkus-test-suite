package io.quarkus.ts.spring.web;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public class AbstractDbIT {
    static final String MARIADB_USER = "user";
    static final String MARIADB_PASSWORD = "user";
    static final String MARIADB_DATABASE = "mydb";
    static final int MARIADB_PORT = 3306;

    @Container(image = "registry.access.redhat.com/rhscl/mariadb-102-rhel7", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static final DefaultService database = new DefaultService()
            .withProperty("MYSQL_USER", MARIADB_USER)
            .withProperty("MYSQL_PASSWORD", MARIADB_PASSWORD)
            .withProperty("MYSQL_DATABASE", MARIADB_DATABASE);

    @QuarkusApplication
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", MARIADB_USER)
            .withProperty("quarkus.datasource.password", MARIADB_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> database.getHost().replace("http", "jdbc:mariadb") + ":" + database.getPort() + "/"
                            + MARIADB_DATABASE);

}
