package io.quarkus.ts.hibernate.reactive;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndJava17;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@DisabledOnFipsAndJava17(reason = "https://github.com/quarkusio/quarkus/issues/40813")
@QuarkusScenario
public class MsSQLDatabaseHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

    @SqlServerContainer
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
