package io.quarkus.qe.hibernate;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlHibernateSpatialIT extends AbstractHibernateSpatialIT {

    static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.84.image}", port = MYSQL_PORT, expectedLog = "ready for connections.* port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mysql"))
    public static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mysql")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

}
