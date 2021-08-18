package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftMssqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static DefaultService mssql = new DefaultService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("mssql.properties")
            .withProperty("quarkus.datasource.username", "sa")
            .withProperty("quarkus.datasource.password", "My1337p@ssworD")
            .withProperty("quarkus.datasource.jdbc.url", "jdbc:sqlserver://mssql:1433;databaseName=mydb");
}
