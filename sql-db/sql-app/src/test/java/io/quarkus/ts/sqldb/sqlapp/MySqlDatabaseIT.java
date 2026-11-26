package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class MySqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.image}", port = MYSQL_PORT, expectedLog = "port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
