package io.quarkus.ts.spring.web.reactive.bootstrap;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.spring.web.reactive.db.MariaDBConstants;

@QuarkusScenario
public class SpringWebQuteReactiveIT extends AbstractSpringWebQuteReactiveIT {

    @Container(image = MariaDBConstants.IMAGE_11, port = MariaDBConstants.PORT, expectedLog = MariaDBConstants.START_LOG_11)
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
