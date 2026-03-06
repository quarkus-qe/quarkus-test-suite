package io.quarkus.ts.jakarta.data;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${mysql.84.image}", port = 3306, expectedLog = "ready for connections.* port: 3306")
    public static final MySqlService database = new MySqlService();

    @QuarkusApplication
    static final RestService app = createApp(database, "mysql");
}
