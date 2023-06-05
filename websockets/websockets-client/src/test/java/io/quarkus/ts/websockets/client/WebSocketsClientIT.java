package io.quarkus.ts.websockets.client;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
public class WebSocketsClientIT {

    // Adding `quarkus-websockets` extension
    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-websockets"))
    static final RestService server = new RestService();

    // Using `quarkus-websockets-client` extension by default
    @QuarkusApplication
    static final RestService client = new RestService()
            .withProperty("app.chat.uri", () -> server.getURI(Protocol.HTTP).toString());

    @Test
    public void smoke() {
        Response response = client.given().get("/user/check/smoke");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("User smoke joined", response.body().asString());
    }

    @Test
    public void push() {
        Response response = client.given().get("/user/push");
        Assertions.assertEquals(200, response.statusCode());
        JsonPath json = response.body().jsonPath();
        List<String> list = json.get();
        Assertions.assertEquals("One", list.get(0));
        Assertions.assertEquals("Two", list.get(1));
        Assertions.assertEquals("Three", list.get(2));
        Assertions.assertEquals("Four", list.get(3));
    }
}
