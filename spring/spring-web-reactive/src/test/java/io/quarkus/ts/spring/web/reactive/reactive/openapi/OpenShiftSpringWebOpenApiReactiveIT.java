package io.quarkus.ts.spring.web.reactive.reactive.openapi;

import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.IMAGE_103;
import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.START_LOG;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftSpringWebOpenApiReactiveIT extends AbstractSpringWebOpenApiReactiveIT {

    @Container(image = IMAGE_103, port = PORT, expectedLog = START_LOG)
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
