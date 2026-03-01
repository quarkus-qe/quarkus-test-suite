package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MariaDBStorageEngineOnlineIT extends AbstractStorageEngineOnlineIT {

    @Container(image = "${mariadb.11.image}", expectedLog = "socket: '.*sock'  port: 3306", mounts = {
            @Mount(from = "mysql-init.sql", to = "/docker-entrypoint-initdb.d/init.sql")
    }, port = 3306, builder = FixedPortResourceBuilder.class)
    static final MariaDbService db = new MariaDbService();

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mariadb"), properties = "storage-engine-online-test.properties")
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mariadb")
            .withProperty("jdbc-url", () -> "jdbc:mariadb://localhost:3306")
            .withProperty("quarkus.datasource.pu1.username", db::getUser)
            .withProperty("quarkus.datasource.pu1.password", db::getPassword)
            .withProperty("quarkus.datasource.pu2.username", db::getUser)
            .withProperty("quarkus.datasource.pu2.password", db::getPassword);

}
