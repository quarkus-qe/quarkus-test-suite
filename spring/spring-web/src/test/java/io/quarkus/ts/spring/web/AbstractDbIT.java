package io.quarkus.ts.spring.web;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public class AbstractDbIT {
    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.102.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

}
