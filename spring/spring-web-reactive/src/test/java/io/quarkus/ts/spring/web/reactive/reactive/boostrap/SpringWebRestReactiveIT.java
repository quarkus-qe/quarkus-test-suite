package io.quarkus.ts.spring.web.reactive.reactive.boostrap;

import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.IMAGE_105;
import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.START_LOG;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class SpringWebRestReactiveIT extends AbstractSpringWebRestReactiveIT {

    @Container(image = IMAGE_105, port = PORT, expectedLog = START_LOG)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.reactive.url",
                    () -> "vertx-reactive:" + database.getHost().replace("http", "mysql") + ":" + database.getPort() + "/"
                            + database.getDatabase());

    @Override
    protected RestService getApp() {
        return app;
    }
}
