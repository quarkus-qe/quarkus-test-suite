package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("fips-incompatible") // disabled as only works in 3.9+
@QuarkusScenario
public class MySqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.57.image}", port = MYSQL_PORT, expectedLog = "port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
