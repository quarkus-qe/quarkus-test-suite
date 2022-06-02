package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class MariaDbDatabaseHibernateReactiveIT extends AbstractDatabaseHibernateReactiveIT {

    private static final String MYSQL_USER = "quarkus_test";
    private static final String MYSQL_PASSWORD = "quarkus_test";
    private static final String MYSQL_DATABASE = "quarkus_test";
    private static final int MYSQL_PORT = 3306;

    // TODO At the time of writing, there is no specific connector for mariadb, so we are using MY SQL driver and service.
    // we need to change this, if this connector will be ever provided Additionally, we need to add an OpenShift test
    @Container(image = "${mariadb.105.image}", port = MYSQL_PORT, expectedLog = "Only MySQL server logs after this point")
    static DefaultService database = new DefaultService()
            .withProperty("MYSQL_USER", MYSQL_USER)
            .withProperty("MYSQL_PASSWORD", MYSQL_PASSWORD)
            .withProperty("MYSQL_DATABASE", MYSQL_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties")
            .withProperty("quarkus.datasource.username", MYSQL_USER)
            .withProperty("quarkus.datasource.password", MYSQL_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url",
                    () -> "vertx-reactive:" + database.getHost().replace("http", "mysql") + ":" + database.getPort() + "/"
                            + MYSQL_DATABASE);

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
