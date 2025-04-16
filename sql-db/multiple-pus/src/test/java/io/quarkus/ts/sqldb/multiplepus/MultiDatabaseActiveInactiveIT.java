package io.quarkus.ts.sqldb.multiplepus;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-5659")
@QuarkusScenario
public class MultiDatabaseActiveInactiveIT extends AbstractMultiDatabaseActiveInactiveIT {

    @Container(image = "${mariadb.11.image}", port = MARIADB_PORT, expectedLog = "socket: '/run/mysqld/mysqld.sock'  port: "
            + MARIADB_PORT)
    static MariaDbService mariadb = new MariaDbService();

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql = new PostgresqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("MARIA_DB_USERNAME", mariadb.getUser())
            .withProperty("MARIA_DB_PASSWORD", mariadb.getPassword())
            .withProperty("MARIA_DB_JDBC_URL", mariadb::getJdbcUrl)
            .withProperty("POSTGRESQL_USERNAME", postgresql.getUser())
            .withProperty("POSTGRESQL_PASSWORD", postgresql.getPassword())
            .withProperty("POSTGRESQL_JDBC_URL", postgresql::getJdbcUrl)
            .withProperty("quarkus.datasource.\"fruits\".active", "false")
            .withProperty("quarkus.datasource.\"vegetables\".active", "false")
            .withProperty("quarkus.hibernate-orm.\"fruits\".active", "false")
            .withProperty("quarkus.hibernate-orm.\"vegetables\".active", "false")
            .withProperty("quarkus.hibernate-orm.\"fungi\".active", "false");

    @Override
    protected RestService getApp() {
        return app;
    }

    @Test
    @Order(10)
    @DisplayName("Activate persistence unit name (fungus) with inactive datasource (vegetable)")
    public void createAndDeleteFungusWhenVegetableDatasourceIsInactive() {
        getApp().stop();
        assertThrows(AssertionError.class, () -> {
            getApp().withProperty("quarkus.datasource.\"fruits\".active", "false")
                    .withProperty("quarkus.datasource.\"vegetables\".active", "false")
                    .withProperty("quarkus.hibernate-orm.\"fruits\".active", "false")
                    .withProperty("quarkus.hibernate-orm.\"vegetables\".active", "false")
                    .withProperty("quarkus.hibernate-orm.\"fungi\".active", "true")
                    .start();
        },
                "Should not start as the fungi hibernate-orm using same datasource which is inactive.");

        getApp().logs().assertContains("Datasource 'vegetables' was deactivated");
    }
}
