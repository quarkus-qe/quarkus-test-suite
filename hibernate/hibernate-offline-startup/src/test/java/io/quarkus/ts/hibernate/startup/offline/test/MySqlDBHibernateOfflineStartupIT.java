package io.quarkus.ts.hibernate.startup.offline.test;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MySqlDBHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @Container(image = "${mysql.80.image}", expectedLog = "Only MySQL server logs after this point", mounts = {
            @Mount(from = "mysql-init.sql", to = "/tmp/init.sql"),
            @Mount(from = "mysql-my-conf.config", to = "/etc/my.cnf.d/my.cnf")
    }, port = 3306, builder = FixedPortResourceBuilder.class)
    static final MySqlService db = new MySqlService().setAutoStart(false);

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mysql"))
    static final RestService app = new RestService()
            .withProperty("jdbc-url", "jdbc:mysql://localhost:3306")
            .withProperty("quarkus.hibernate-orm.multitenant", "DATABASE")
            .withProperty("quarkus.datasource.app_scope_credentials.jdbc.url", "${jdbc-url}/app_scope_db")
            .withProperty("quarkus.datasource.req_scope_credentials.jdbc.url", "${jdbc-url}/req_scope_db")
            .withProperty("quarkus.datasource.own_connection_provider.jdbc.url", "${jdbc-url}/own_conn_db");

}
