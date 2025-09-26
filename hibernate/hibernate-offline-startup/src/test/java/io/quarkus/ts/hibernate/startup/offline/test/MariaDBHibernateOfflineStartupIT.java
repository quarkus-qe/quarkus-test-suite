package io.quarkus.ts.hibernate.startup.offline.test;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class MariaDBHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    @Container(image = "${mariadb.11.image}", expectedLog = "socket: '.*/mysql.*sock'  port: 3306", mounts = {
            @Mount(from = "mysql-init.sql", to = "/docker-entrypoint-initdb.d/init.sql")
    }, port = 3306, builder = FixedPortResourceBuilder.class)
    static final MariaDbService db = new MariaDbService().setAutoStart(false);

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-mariadb"))
    static final RestService app = new RestService()
            .withProperty("jdbc-url", "jdbc:mariadb://localhost:3306")
            .withProperty("quarkus.hibernate-orm.multitenant", "DATABASE")
            .withProperty("quarkus.datasource.app_scope_credentials.jdbc.url", "${jdbc-url}/app_scope_db")
            .withProperty("quarkus.datasource.req_scope_credentials.jdbc.url", "${jdbc-url}/req_scope_db")
            .withProperty("quarkus.datasource.own_connection_provider.jdbc.url", "${jdbc-url}/own_conn_db");

    @Test
    void testDialectTuning() {
        final String tenant = "req_scope_credentials";
        app.given()
                .pathParam("tenant", tenant)
                .header("username", db.getUser())
                .header("password", db.getPassword())
                .get("/default-pu/newspapers/{tenant}/dialect/max-varchar-length")
                .then()
                .statusCode(200)
                // (65,535 bytes - 2 bytes) / (3 max bytes) ~= 21844
                .body(is("21844"));
    }
}
