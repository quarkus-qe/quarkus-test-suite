package io.quarkus.ts.spring.web.reactive.reactive.boostrap;

import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.IMAGE_10;
import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.reactive.reactive.MariaDBConstants.START_LOG_10;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SpringWebQuteReactiveIT extends AbstractSpringWebQuteReactiveIT {

    @Container(image = IMAGE_10, port = PORT, expectedLog = START_LOG_10)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.reactive.url",
                    () -> "vertx-reactive:" + database.getHost().replace("http", "mysql") + ":" + database.getPort() + "/"
                            + database.getDatabase());

    @Override
    public RestService getApp() {
        return app;
    }
}
