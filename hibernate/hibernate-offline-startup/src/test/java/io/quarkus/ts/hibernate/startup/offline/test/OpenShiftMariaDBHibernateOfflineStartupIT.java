package io.quarkus.ts.hibernate.startup.offline.test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;

@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
@OpenShiftScenario
public class OpenShiftMariaDBHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @Container(image = "${mariadb.1011.image}", expectedLog = "socket: '.*/mysql.*sock'  port: 3306", mounts = {
            @Mount(from = "mysql-init.sql", to = "/tmp/init.sql"),
            @Mount(from = "mysql-my-conf.config", to = "/etc/my.cnf.d/custom.cnf")
    }, port = 3306)
    static final MariaDbService db = new MariaDbService().setAutoStart(false);

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mariadb"))
    static final RestService app = new RestService()
            .withProperty("jdbc-url", "jdbc:mariadb://db:3306")
            .withProperty("quarkus.hibernate-orm.multitenant", "DATABASE")
            .withProperty("quarkus.datasource.app_scope_credentials.jdbc.url", "${jdbc-url}/app_scope_db")
            .withProperty("quarkus.datasource.req_scope_credentials.jdbc.url", "${jdbc-url}/req_scope_db")
            .withProperty("quarkus.datasource.own_connection_provider.jdbc.url", "${jdbc-url}/own_conn_db");

}
