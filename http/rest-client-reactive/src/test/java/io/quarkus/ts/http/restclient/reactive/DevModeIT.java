package io.quarkus.ts.http.restclient.reactive;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.json.Author;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;

@QuarkusScenario
public class DevModeIT {

    @DevModeQuarkusApplication(properties = "modern.properties")
    static RestService app = new RestService();

    @Test
    @Tag("https://issues.redhat.com/browse/QUARKUS-5616")
    public void deserializeBoolean() {
        Response response = app.given()
                .contentType(ContentType.JSON)
                .body(new Author("Ernest Hemingway", true))
                .post("/client/book/author/books/");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        ResponseBody body = response.getBody();
        assertTrue(body.asString().contains("The Old Man and the Sea"));
    }

    @Test
    @Tag("https://issues.redhat.com/browse/QUARKUS-5616")
    public void serializeBoolean() {
        Response response = app.given().get("/client/book/author/info/?author=Hemingway");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        ResponseBody body = response.getBody();
        JsonPath json = body.jsonPath();
        assertEquals("Hemingway", json.getString("name"));
        assertTrue(json.getBoolean("gotNobelPrize"));
    }

    @Test
    @DisabledOnNative(reason = "This endpoint uses reflection which is not accessible in native mode")
    public void verifyGeneratedClasses() {
        app.given()
                .get("/meta/class/io.quarkus.ts.http.restclient.reactive.json.Author$quarkusjacksonserializer")
                .then()
                .statusCode(200)
                .body(is("io.quarkus.ts.http.restclient.reactive.json.Author$quarkusjacksonserializer"));
        app.given()
                .get("/meta/class/io.quarkus.ts.http.restclient.reactive.json.Author$quarkusjacksondeserializer")
                .then()
                .statusCode(200)
                .body(is("io.quarkus.ts.http.restclient.reactive.json.Author$quarkusjacksondeserializer"));
    }
}
