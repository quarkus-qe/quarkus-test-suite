package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

// TODO: enable with next Hibernate Reactive bump in Quarkus main
@Disabled("https://github.com/quarkusio/quarkus/issues/32102#issuecomment-1482501348")
@QuarkusScenario
public class MsSQLDatabaseHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

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

}
