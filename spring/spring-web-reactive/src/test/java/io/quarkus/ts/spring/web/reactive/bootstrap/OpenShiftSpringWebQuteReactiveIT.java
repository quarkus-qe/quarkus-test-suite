package io.quarkus.ts.spring.web.reactive.bootstrap;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.spring.web.reactive.MariaDBConstants;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftSpringWebQuteReactiveIT extends AbstractSpringWebQuteReactiveIT {

    @Container(image = MariaDBConstants.IMAGE_103, port = MariaDBConstants.PORT, expectedLog = MariaDBConstants.START_LOG)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl);

    @Override
    public RestService getApp() {
        return app;
    }
}
