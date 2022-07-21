package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

// TODO https://github.com/quarkus-qe/quarkus-test-suite/issues/756
@Tag("fips-incompatible") // native-mode
@QuarkusScenario
public class MySQLDatabaseHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

    private static final String MYSQL_USER = "quarkus_test";
    private static final String MYSQL_PASSWORD = "quarkus_test";
    private static final String MYSQL_DATABASE = "quarkus_test";
    private static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.80.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static MySqlService database = new MySqlService()
            .withUser(MYSQL_USER)
            .withPassword(MYSQL_PASSWORD)
            .withDatabase(MYSQL_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", MYSQL_USER)
            .withProperty("quarkus.datasource.password", MYSQL_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    @Disabled // due to https://github.com/quarkusio/quarkus/issues/22254
    public void convertValue() {

    }

    @Override
    @Disabled // due to https://github.com/quarkusio/quarkus/issues/22254
    public void setConvertedValue() {

    }
}
