package io.quarkus.ts.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
//TODO: enable disabled methods then the issue will be fixed
public class MsSQLDatabaseIT extends AbstractReactiveDatabaseIT {

    private static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mssql.properties")
            .withProperty("quarkus.datasource.username", database::getUser)
            .withProperty("quarkus.datasource.password", database::getPassword)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Test
    @Override
    @Disabled("https://github.com/quarkusio/quarkus/issues/19539")
    public void connectToUniEndpoint() {
        // MSSQL hibernate has some problems with extended Latin
    }

    @Test
    @Override
    @Disabled("https://github.com/quarkusio/quarkus/issues/19539")
    public void connectToMultiEndpoint() {
        // MSSQL hibernate has some problems with extended Latin
    }
}
