package io.quarkus.ts.http.graphql;

import static io.quarkus.ts.http.graphql.Utils.createQuery;
import static io.quarkus.ts.http.graphql.Utils.sendGetQuery;
import static io.quarkus.ts.http.graphql.Utils.sendQuery;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
@Tag("QUARKUS-2485")
public class GraphQLDisabledGetIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.smallrye-graphql.http.get.enabled", "false");

    @Test
    public void singlePost() {
        final String query = createQuery("friend(name:\"Aristotle\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend.name"));
    }

    @Test
    public void emptyGet() {
        Response response = given().basePath("graphql")
                .contentType("application/json")
                .get();
        Assertions.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.statusCode());
    }

    @Test
    public void singleGet() {
        final Response response = sendGetQuery("friend(name:\"Aristotle\"){name}");
        Assertions.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.statusCode());
    }

    @Test
    public void singleGetReactive() {
        final Response response = sendGetQuery("friend_reactive(name:\"Aristotle\"){name}");
        Assertions.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.statusCode());
    }
}
