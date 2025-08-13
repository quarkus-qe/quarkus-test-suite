package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@Tag("QUARKUS-6242")
@Tag("fips-incompatible") // MSSQL works with BC JSSE FIPS which is not native-compatible, we test FIPS elsewhere
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@QuarkusScenario
public class MsSQLHibernateValidatorAutoIT extends AbstractHibernateValidatorIT {

    @SqlServerContainer
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mssql")
            .withProperty("quarkus.datasource.username", database::getUser)
            .withProperty("quarkus.datasource.password", database::getPassword)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    protected RestService getApp() {
        return app;
    }
}
