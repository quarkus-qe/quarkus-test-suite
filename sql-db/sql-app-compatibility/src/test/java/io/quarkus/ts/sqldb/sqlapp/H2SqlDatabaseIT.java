package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledIfSystemProperty(named = "quarkus.package.type", matches = "native", disabledReason = "H2 database compiled into a native-image is only functional as a client")
public class H2SqlDatabaseIT extends AbstractSqlDatabaseIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("h2.properties");
}
