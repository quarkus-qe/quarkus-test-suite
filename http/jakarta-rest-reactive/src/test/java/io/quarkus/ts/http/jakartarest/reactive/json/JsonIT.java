package io.quarkus.ts.http.jakartarest.reactive.json;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@Tag("QUARKUS-1075")
@QuarkusScenario
public class JsonIT {
    private static final String BASE_PATH = "/json";
    private static final String CUSTOM_JSON = String.format("{id:%d,name:\"%s\"}", JsonResource.USER_ID,
            JsonResource.USER_NAME);

    @Test
    public void shouldGetUserPublic() {
        final User user = getUser("/public");
        assertNull(user.id);
    }

    @Test
    public void shouldGetUserPrivate() {
        final User user = getUser("/private");
        assertNotNull(user.id);
    }

    @Test
    public void shouldGetCustomJson() {
        final String stringBody = whenGet("/custom").extract().body().asString();
        assertEquals(CUSTOM_JSON, stringBody);
    }

    private User getUser(String path) {
        final User user = whenGet(path).extract().body().as(User.class);
        assertNotNull(user.name);
        return user;
    }

    private ValidatableResponse whenGet(String path) {
        return given()
                .get(BASE_PATH + path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON);
    }
}
