package io.quarkus.ts.sqldb.panacheflyway;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

public abstract class PanacheWithFlywayBaseIT {
    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.84.image}", port = MYSQL_PORT, expectedLog = "ready for connections.* port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.flyway.schemas", database.getDatabase());
}
