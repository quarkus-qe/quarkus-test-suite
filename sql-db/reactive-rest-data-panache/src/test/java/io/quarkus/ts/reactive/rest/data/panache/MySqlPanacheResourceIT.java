package io.quarkus.ts.reactive.rest.data.panache;

import java.util.Map;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlPanacheResourceIT extends AbstractPanacheResourceIT {

    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperties(() -> {
                boolean fipsEnabledEnv = System.getProperty("excludedGroups", "").contains("fips-incompatible");
                if (fipsEnabledEnv) {
                    // TODO: drop when Quarkus migrates to Vert.x with https://github.com/eclipse-vertx/vertx-sql-client/issues/1539
                    // see https://github.com/eclipse-vertx/vertx-sql-client/issues/1436#issuecomment-3109720406
                    return Map.of(
                            "quarkus.datasource.reactive.mysql.ssl-mode", "preferred",
                            "quarkus.datasource.reactive.trust-all", "true");
                }
                return Map.of();
            });
}
