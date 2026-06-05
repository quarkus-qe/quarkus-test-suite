package io.quarkus.ts.jakarta.data.security;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MariaDbJakartaDataSecurityIT extends AbstractJakartaDataSecurityIT {

    @Container(image = "${mariadb.11.image}", port = 3306, expectedLog = "socket: '.*sock'  port: 3306")
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    static final RestService app = createApp(database, "mariadb");
}
