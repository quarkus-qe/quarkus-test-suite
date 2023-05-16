package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@DisabledOnNative(reason = "Works in native, but we only provide DEV support")
@QuarkusScenario
public class H2SqlDatabaseIT extends AbstractSqlDatabaseIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("h2.properties");
}
