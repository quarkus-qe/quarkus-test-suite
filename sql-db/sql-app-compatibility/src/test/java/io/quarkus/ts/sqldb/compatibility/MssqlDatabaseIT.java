package io.quarkus.ts.sqldb.compatibility;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MssqlDatabaseIT extends AbstractSqlDatabaseIT {

    private static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperties("mssql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
