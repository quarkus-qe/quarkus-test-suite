package io.quarkus.ts.spring.web.reactive.openapi;

import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.IMAGE_105;
import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.START_LOG_105;

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
public class OpenShiftSpringWebOpenApiIT extends AbstractSpringWebOpenApiIT {

    @Container(image = IMAGE_105, port = PORT, expectedLog = START_LOG_105)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Override
    public RestService getApp() {
        return app;
    }
}
