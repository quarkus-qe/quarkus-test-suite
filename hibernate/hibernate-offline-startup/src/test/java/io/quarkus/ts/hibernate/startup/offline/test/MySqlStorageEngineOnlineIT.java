package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlStorageEngineOnlineIT extends AbstractStorageEngineOnlineIT {

    @Container(image = "${mysql.80.image}", expectedLog = "Only MySQL server logs after this point", mounts = {
            @Mount(from = "mysql-init.sql", to = "/tmp/init.sql"),
            @Mount(from = "mysql-my-conf.config", to = "/etc/my.cnf.d/my.cnf")
    }, port = 3306, builder = FixedPortResourceBuilder.class)
    static final MySqlService db = new MySqlService();

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mysql"), properties = "storage-engine-online-test.properties")
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mysql")
            .withProperty("jdbc-url", "jdbc:mysql://localhost:3306")
            .withProperty("quarkus.datasource.pu1.username", db::getUser)
            .withProperty("quarkus.datasource.pu1.password", db::getPassword)
            .withProperty("quarkus.datasource.pu2.username", db::getUser)
            .withProperty("quarkus.datasource.pu2.password", db::getPassword);

}