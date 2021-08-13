package io.quarkus.qe.sqldb.panacheflyway;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public class BaseIT {
    static final String MYSQL_USER = "user";
    static final String MYSQL_PASSWORD = "user";
    static final String MYSQL_DATABASE = "test";
    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.57.image}", port = MYSQL_PORT, expectedLog = "ready for connections")
    static DefaultService database = new DefaultService()
            .withProperty("MYSQL_USER", MYSQL_USER)
            .withProperty("MYSQL_PASSWORD", MYSQL_PASSWORD)
            .withProperty("MYSQL_ROOT_PASSWORD", MYSQL_PASSWORD)
            .withProperty("MYSQL_DATABASE", MYSQL_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", MYSQL_USER)
            .withProperty("quarkus.datasource.password", MYSQL_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> database.getHost().replace("http", "jdbc:mysql") + ":" + database.getPort() + "/"
                            + MYSQL_DATABASE);
}
