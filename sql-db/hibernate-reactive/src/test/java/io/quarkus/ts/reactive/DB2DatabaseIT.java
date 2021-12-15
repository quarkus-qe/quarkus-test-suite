package io.quarkus.ts.reactive;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class DB2DatabaseIT extends AbstractReactiveDatabaseIT {

    private static final String DB2_USER = "test";
    private static final String DB2_PASSWORD = "test";
    private static final String DB2_DATABASE = "amadeus";

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static Db2Service db2 = new Db2Service()
            .withUser(DB2_USER)
            .withPassword(DB2_PASSWORD)
            .withDatabase(DB2_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("db2.properties")
            .withProperty("quarkus.datasource.username", DB2_USER)
            .withProperty("quarkus.datasource.password", DB2_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url", db2::getJdbcUrl)
            .withProperty("quarkus.datasource.reactive.url", db2::getReactiveUrl);

    @Override
    protected RestService getApp() {
        return app;
    }
}
