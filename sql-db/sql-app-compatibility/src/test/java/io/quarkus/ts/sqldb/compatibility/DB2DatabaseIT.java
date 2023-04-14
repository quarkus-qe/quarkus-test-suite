package io.quarkus.ts.sqldb.compatibility;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative(reason = "Compatibility mode check in JVM mode is enough for this DB")
@Tag("fips-incompatible") // Reported in https://github.com/IBM/Db2/issues/43
public class DB2DatabaseIT extends AbstractSqlDatabaseIT {

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static Db2Service db2 = new Db2Service();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("db2_app.properties")
            .withProperty("quarkus.datasource.username", db2.getUser())
            .withProperty("quarkus.datasource.password", db2.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", db2::getJdbcUrl);
}
