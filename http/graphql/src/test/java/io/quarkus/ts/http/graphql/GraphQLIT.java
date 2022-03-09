package io.quarkus.ts.http.graphql;

import static io.quarkus.ts.http.graphql.Utils.createMutation;
import static io.quarkus.ts.http.graphql.Utils.createQuery;
import static io.quarkus.ts.http.graphql.Utils.sendQuery;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
public class GraphQLIT {

    @Test
    public void recursive() {
        final String query = createQuery("philosophers{name,friend{name,friend{name}}}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.philosophers[0].name"));
        Assertions.assertEquals("Aristotle", json.getString("data.philosophers[0].friend.name"));
        Assertions.assertEquals("Plato", json.getString("data.philosophers[0].friend.friend.name"));
    }

    @Test
    public void single() {
        final String query = createQuery("friend(name:\"Aristotle\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend.name"));
    }

    @Test
    public void reactive() {
        final String query = createQuery("friend_r(name:\"Aristotle\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend_r.name"));
    }

    @Test
    public void create() {
        final String query = createMutation("create(name:\"Diogen\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Diogen", json.getString("data.create.name"));
    }

    @Test
    public void emptyGet() {
        Response response = given().basePath("graphql")
                .contentType("application/json")
                .post();
        Assertions.assertNotEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.statusCode());
        Assertions.assertNotEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.statusCode());
        Assertions.assertNotEquals(HttpStatus.SC_NO_CONTENT, response.statusCode());
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Tag("QUARKUS-1537")
    public void emptyPost() {
        Response response = given().basePath("graphql")
                .contentType("application/json")
                .post();
        Assertions.assertNotEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.statusCode());
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.statusCode());
    }
}
