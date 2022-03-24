package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class ClassicPropertiesIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("classic.properties");

    @Test
    public void shouldGetBookFromRestClientJson() {
        Response response = app.given().with().pathParam("id", "123")
                .get("/client/{id}/book/json");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Title in Json: 123", response.jsonPath().getString("title"));
    }
}
