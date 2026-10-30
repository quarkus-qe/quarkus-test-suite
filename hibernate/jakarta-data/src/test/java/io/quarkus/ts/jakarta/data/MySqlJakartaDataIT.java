package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

// TODO Quarkus 4: Jakarta Data repositories without dataStore register named queries in the
// default (unnamed) PU at build time. With the named-PU config (using active toggling per profile),
// the default PU doesn't exist and the queries are not found. The RepositoryBeansProducer
// runtime redirect no longer works because query registration happens at build time.
@Disabled("TODO Quarkus 4: Jakarta Data named query registration requires default PU")
@QuarkusScenario
public class MySqlJakartaDataIT extends AbstractJakartaDataIT {

    @Container(image = "${mysql.image}", port = 3306, expectedLog = "ready for connections.* port: 3306")
    public static final MySqlService database = new MySqlService();

    @QuarkusApplication
    static final RestService app = createApp(database, "mysql");
}
