package io.quarkus.ts.hibernate.startup.offline.test;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DatabaseService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.hibernate.startup.offline.pu.defaults.orm.Article;
import io.quarkus.ts.hibernate.startup.offline.pu.defaults.rest.DatabaseManagementResource.DatabaseCredentials;
import io.restassured.http.ContentType;

public abstract class AbstractHibernateOfflineStartupIT {

    static final String ONLINE_STARTUP_FAILED_MESSAGE = "Could not retrieve the database version to check it is at least";

    @LookupService
    static DatabaseService<?> db;

    @LookupService
    static RestService app;

    @Test
    void testApplicationScopedCredentialsOnFirstRequest() {
        final String tenant = "app_scope_credentials";
        startDbIfNotRunning();
        // upload application scoped credentials
        app.given()
                .contentType(ContentType.JSON)
                .pathParam("tenant", tenant)
                .body(new DatabaseCredentials(db.getUser(), db.getPassword()))
                .post("/default-pu/database-management/{tenant}/store-app-scoped-credentials")
                .then()
                .statusCode(204);
        // create database scheme
        // awaitility are used because the very first request needs database ready
        untilAsserted(() -> app.given()
                .pathParam("tenant", tenant)
                .post("/default-pu/database-management/{tenant}/create-schema")
                .then()
                .statusCode(204));

        // make sure that the tenant schema is empty
        app.given()
                .pathParam("tenant", tenant)
                .get("/default-pu/newspapers/{tenant}/article/count")
                .then()
                .statusCode(200)
                .body(is("0"));
        // creates Article entity instance
        final String articleName = "My %s article".formatted(tenant);
        Long entityId = Long.parseLong(app.given()
                .contentType(ContentType.JSON)
                .pathParam("tenant", tenant)
                .body(new Article(null, articleName))
                .post("/default-pu/newspapers/{tenant}/article")
                .then()
                .statusCode(200)
                .extract().asString());
        // retrieves Article entity and asserts it
        app.given()
                .pathParam("id", entityId)
                .pathParam("tenant", tenant)
                .get("/default-pu/newspapers/{tenant}/article/{id}")
                .then()
                .statusCode(200)
                .body("id", is(entityId.intValue()))
                .body("name", is(articleName));
    }

    @Test
    void testRequestScopedCredentialsOnFirstRequest() {
        final String tenant = "req_scope_credentials";
        startDbIfNotRunning();
        // create database scheme
        // awaitility are used because the very first request needs database ready
        untilAsserted(() -> app.given()
                .pathParam("tenant", tenant)
                .header("username", db.getUser())
                .header("password", db.getPassword())
                .post("/default-pu/database-management/{tenant}/create-schema")
                .then()
                .statusCode(204));

        // make sure that the tenant schema is empty
        app.given()
                .pathParam("tenant", tenant)
                .header("username", db.getUser())
                .header("password", db.getPassword())
                .get("/default-pu/newspapers/{tenant}/article/count")
                .then()
                .statusCode(200)
                .body(is("0"));
        // no credentials must mean no connection
        app.given()
                .pathParam("tenant", tenant)
                .get("/default-pu/newspapers/{tenant}/article/count")
                .then()
                .statusCode(500);
        // creates Article entity instance
        final String articleName = "My %s article".formatted(tenant);
        Long entityId = Long.parseLong(app.given()
                .contentType(ContentType.JSON)
                .pathParam("tenant", tenant)
                .header("username", db.getUser())
                .header("password", db.getPassword())
                .body(new Article(null, articleName))
                .post("/default-pu/newspapers/{tenant}/article")
                .then()
                .statusCode(200)
                .extract().asString());
        // retrieves Article entity and asserts it
        app.given()
                .pathParam("id", entityId)
                .pathParam("tenant", tenant)
                .header("username", db.getUser())
                .header("password", db.getPassword())
                .get("/default-pu/newspapers/{tenant}/article/{id}")
                .then()
                .statusCode(200)
                .body("id", is(entityId.intValue()))
                .body("name", is(articleName));
    }

    @Test
    void testProgrammaticallyResolveConnection() {
        final String tenant = "own_connection_provider";
        startDbIfNotRunning();
        // create database scheme
        // awaitility are used because the very first request needs database ready
        untilAsserted(() -> app.given()
                .pathParam("tenant", tenant)
                .header("connection-user", db.getUser())
                .header("connection-password", db.getPassword())
                .post("/own-connection-provider-pu/database-management/{tenant}/create-schema")
                .then()
                .statusCode(204));

        // no credentials must mean no result
        app.given()
                .pathParam("tenant", tenant)
                .get("/own-connection-provider-pu/newspapers/{tenant}/article/count")
                .then()
                .statusCode(500);

        // make sure that the tenant schema is empty
        app.given()
                .pathParam("tenant", tenant)
                .header("connection-user", db.getUser())
                .header("connection-password", db.getPassword())
                .get("/own-connection-provider-pu/newspapers/{tenant}/article/count")
                .then()
                .statusCode(200)
                .body(is("0"));
        // creates Article entity instance
        final String articleName = "My %s article".formatted(tenant);
        Long entityId = Long.parseLong(app.given()
                .contentType(ContentType.JSON)
                .pathParam("tenant", tenant)
                .header("connection-user", db.getUser())
                .header("connection-password", db.getPassword())
                .body(new Article(null, articleName))
                .post("/own-connection-provider-pu/newspapers/{tenant}/article")
                .then()
                .statusCode(200)
                .extract().asString());
        // retrieves Article entity and asserts it
        app.given()
                .pathParam("id", entityId)
                .pathParam("tenant", tenant)
                .header("connection-user", db.getUser())
                .header("connection-password", db.getPassword())
                .get("/own-connection-provider-pu/newspapers/{tenant}/article/{id}")
                .then()
                .statusCode(200)
                .body("id", is(entityId.intValue()))
                .body("name", is(articleName));
    }

    protected static void startDbIfNotRunning() {
        if (!db.isRunning()) {
            testApplicationDidNotUseDatabase();
            db.start();
        }
    }

    private static void testApplicationDidNotUseDatabase() {
        app.logs().assertDoesNotContain(ONLINE_STARTUP_FAILED_MESSAGE);
    }
}
