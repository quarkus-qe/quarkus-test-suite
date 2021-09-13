package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MSsqlHandlerIT extends CommonTestCases {
    private static final String MSSQL_PASSWORD = "QuArKuS_tEsT";
    private static final String DATABASE = "msdb";
    private static final int MSSQL_PORT = 1433;

    //fixme Replace with SqlServerService when https://github.com/quarkus-qe/quarkus-test-framework/issues/251 will be solved
    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static DefaultService mssql = new DefaultService()
            .withProperty("ACCEPT_EULA", "Y")
            .withProperty("SA_PASSWORD", MSSQL_PASSWORD);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.mssql.jdbc.url",
                    () -> mssql.getHost().replace("http", "jdbc:sqlserver") + ":" +
                            mssql.getPort() + ";databaseName=" + DATABASE)
            .withProperty("quarkus.datasource.mssql.reactive.url",
                    () -> mssql.getHost().replace("http", "sqlserver") + ":" +
                            mssql.getPort() + "/" + DATABASE)
            .withProperty("app.selected.db", "mssql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mssql.migrate-at-start", "true");
}
