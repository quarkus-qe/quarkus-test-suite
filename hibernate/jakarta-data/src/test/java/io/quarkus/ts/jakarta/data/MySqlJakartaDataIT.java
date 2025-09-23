package io.quarkus.ts.jakarta.data;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${mysql.80.image}", port = 3306, expectedLog = "Only MySQL server logs after this point")
    public static final MySqlService database = new MySqlService();

    @QuarkusApplication
    static final RestService app = createApp(database, "mysql");
}
