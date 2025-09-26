package io.quarkus.ts.hibernate.startup.offline.test;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.logging.Log;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.ts.hibernate.startup.offline.pu.defaults.rest.DatabaseManagementResource;
import io.restassured.http.ContentType;

@QuarkusScenario
public class DevModeHibernateOfflineStartupIT extends AbstractHibernateOfflineStartupIT {

    private static final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";

    @Container(image = "${postgresql.latest.image}", port = 5432, expectedLog = "listening on IPv4 address", builder = FixedPortResourceBuilder.class)
    static final PostgresqlService db = new PostgresqlService().setAutoStart(false);

    @DevModeQuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-jdbc-postgresql"))
    static final DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperty("jdbc-url", "jdbc:postgresql://localhost:5432/mydb");

    @Test
    void testHotReload() {
        // when all the test methods are run, this is not executed in the current execution order
        // but if run independently, we need to run this first in order to assert that offline startup worked
        startDbIfNotRunning();

        // stop database, turn off the offline startup and expect a failure
        Log.info("Stopping database");
        db.stop();
        Log.info("Turning off the offline startup feature");
        app.modifyFile(APPLICATION_PROPERTIES, props -> props.replace("quarkus.hibernate-orm.database.start-offline=true",
                "quarkus.hibernate-orm.database.start-offline=false"));
        Log.info("Waiting for application to get ready");
        AwaitilityUtils
                .untilAsserted(() -> app.given().get("/ping/start-offline").then().statusCode(200).body(is("pong: false")));
        // this should happen due to validation on startup
        // however until the https://github.com/quarkusio/quarkus/issues/50210 is fixed, it can always happen
        app.logs().assertContains("Could not obtain connection to query JDBC database");

        // now test recovery when the offline startup is re-enabled
        Log.info("Enabling the offline startup feature");
        app.modifyFile(APPLICATION_PROPERTIES, props -> props.replace("quarkus.hibernate-orm.database.start-offline=false",
                "quarkus.hibernate-orm.database.start-offline=true"));
        Log.info("Waiting for application to get ready");
        AwaitilityUtils
                .untilAsserted(() -> app.given().get("/ping/start-offline").then().statusCode(200).body(is("pong: true")));
        final String tenant = "app_scope_credentials";
        // upload application scoped credentials
        app.given()
                .contentType(ContentType.JSON)
                .pathParam("tenant", tenant)
                .body(new DatabaseManagementResource.DatabaseCredentials(db.getUser(), db.getPassword()))
                .post("/default-pu/database-management/{tenant}/store-app-scoped-credentials")
                .then()
                .statusCode(204);
        // check that database is not available
        app.given()
                .pathParam("tenant", tenant)
                .get("/ping/database/{tenant}")
                .then().statusCode(500);
        // by now we already asserted that prior to the disabled offline startup, this message wasn't logged
        app.logs().assertContains(ONLINE_STARTUP_FAILED_MESSAGE);

        Log.info("Starting database to test the offline startup feature");
        db.start();
        AwaitilityUtils.untilAsserted(() -> app.given()
                .pathParam("tenant", tenant)
                .get("/ping/database/{tenant}")
                .then()
                .statusCode(200)
                .body(is("pong: true")));
    }

}
