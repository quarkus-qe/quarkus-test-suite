package io.quarkus.ts.spring.web.reactive.openapi;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.spring.web.reactive.MariaDBConstants;

@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringWebOpenApiReactiveIT extends AbstractSpringWebOpenApiReactiveIT {

    @Container(image = MariaDBConstants.IMAGE_10, port = MariaDBConstants.PORT, expectedLog = MariaDBConstants.START_LOG_10)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Override
    protected RestService getApp() {
        return app;
    }
}
