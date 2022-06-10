package io.quarkus.ts.spring.web.reactive.boostrap;

import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.IMAGE_105;
import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.reactive.MariaDBConstants.START_LOG_105;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftSpringWebQuteIT extends AbstractSpringWebQuteIT {

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
