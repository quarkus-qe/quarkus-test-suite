package io.quarkus.ts.http;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusScenario
public class LargeStaticResourceIT {
    private static final int TWO_SECONDS = 2000;

    @Test
    public void testMainPageAvailability() {
        Response response = given().when().get("/");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Assertions.assertEquals("Howdy!", response.body().asString().strip());
    }

    @Test
    public void testBigFileAvailability() throws IOException {
        URI bigFileURL = URI.create(RestAssured.baseURI + ":" + RestAssured.port + "/big-file");
        HttpURLConnection connection = (HttpURLConnection) bigFileURL.toURL().openConnection();
        connection.setRequestMethod("HEAD");
        connection.setConnectTimeout(TWO_SECONDS);
        connection.connect();
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode);
    }
}
