package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftMssqlDatabaseIT extends AbstractSqlDatabaseIT {
    private static final String MSSQL_PASSWORD = "QuArKuS_tEsT";
    private static final String DATABASE = "msdb";

    static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    //fixme Replace with SqlServerService when https://github.com/quarkus-qe/quarkus-test-framework/issues/251 will be solved
    static DefaultService mssql = new DefaultService()
            .withProperty("ACCEPT_EULA", "Y")
            .withProperty("SA_PASSWORD", MSSQL_PASSWORD);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperties("mssql.properties")
            .withProperty("quarkus.datasource.username", "sa")
            .withProperty("quarkus.datasource.password", MSSQL_PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> mssql.getHost().replace("http", "jdbc:sqlserver") + ":" +
                                  mssql.getPort() + ";databaseName=" + DATABASE);
}
