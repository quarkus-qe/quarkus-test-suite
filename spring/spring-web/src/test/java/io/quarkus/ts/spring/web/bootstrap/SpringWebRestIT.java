package io.quarkus.ts.spring.web.bootstrap;

import static io.quarkus.ts.spring.web.db.MariaDBConstants.IMAGE_11;
import static io.quarkus.ts.spring.web.db.MariaDBConstants.PORT;
import static io.quarkus.ts.spring.web.db.MariaDBConstants.START_LOG_11;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SpringWebRestIT extends AbstractSpringWebRestIT {

    @Container(image = IMAGE_11, port = PORT, expectedLog = START_LOG_11)
    static final MariaDbService database = new MariaDbService();

    @QuarkusApplication
    private static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Override
    protected RestService getApp() {
        return app;
    }
}
