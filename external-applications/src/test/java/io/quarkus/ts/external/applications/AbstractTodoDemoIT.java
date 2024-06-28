package io.quarkus.ts.external.applications;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.restassured.http.ContentType;

@DisabledOnNative(reason = "This scenario is using uber-jar, so it's incompatible with Native")
public abstract class AbstractTodoDemoIT {
    protected static final String TODO_REPO = "https://github.com/quarkusio/todo-demo-app.git";
    protected static final String VERSIONS = "-Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID} -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION} ";
    protected static final String DEFAULT_OPTIONS = " -DskipTests=true " + VERSIONS;

    protected abstract RestService getApp();

    protected abstract RestService getReplaced();

    @Test
    public void startsSuccessfully() {
        getApp().given()
                .contentType(ContentType.JSON)
                .body("{\"title\": \"Use Quarkus\", \"order\": 1, \"url\": \"https://quarkus.io\"}")
                .post("/api")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void replacedStartsSuccessfully() {
        getReplaced().given()
                .accept(ContentType.JSON)
                .get("/api")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$.size()", is(1))
                .body("title[0]", is("Use Quarkus"));
    }
}
