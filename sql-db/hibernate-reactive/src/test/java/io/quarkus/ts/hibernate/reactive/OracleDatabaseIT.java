package io.quarkus.ts.hibernate.reactive;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class OracleDatabaseIT extends AbstractDatabaseHibernateReactiveIT {

    private static final String ORACLE_USER = "quarkus_test";
    private static final String ORACLE_PASSWORD = "quarkus_test";
    private static final String ORACLE_DATABASE = "quarkus_test";
    private static final int ORACLE_PORT = 1521;

    @Container(image = "${oracle.image}", port = ORACLE_PORT, expectedLog = "DATABASE IS READY TO USE!")
    static OracleService database = new OracleService()
            .with(ORACLE_USER, ORACLE_PASSWORD, ORACLE_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("oracle.properties")
            .withProperty("quarkus.datasource.username", ORACLE_USER)
            .withProperty("quarkus.datasource.password", ORACLE_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Override
    protected RestService getApp() {
        return app;
    }
}
