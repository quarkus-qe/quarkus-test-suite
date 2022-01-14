package io.quarkus.ts.spring.web.reactive.boostrap;

import io.quarkus.test.bootstrap.MariaDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftBookResourceReactiveIT extends BookResourceReactiveIT {

    private static final String API_ROOT = "/api/books";

    static final int MARIADB_PORT = 3306;

    @Container(image = "${mariadb.103.image}", port = MARIADB_PORT, expectedLog = "Only MySQL server logs after this point")
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
