package io.quarkus.ts.http.graphql;

import static io.quarkus.ts.http.graphql.Utils.createMutation;
import static io.quarkus.ts.http.graphql.Utils.createQuery;
import static io.quarkus.ts.http.graphql.Utils.sendGetQuery;
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
    @Tag("QUARKUS-2485")
    public void reactive() {
        final String query = createQuery("friend_reactive(name:\"Aristotle\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend_reactive.name"));
    }

    @Test
    public void create() {
        final String query = createMutation("create(name:\"Diogen\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Diogen", json.getString("data.create.name"));
    }

    @Test
    @Tag("QUARKUS-2485")
    public void createReactive() {
        final String query = createMutation("create_reactive(name:\"Parmenides\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Parmenides", json.getString("data.create_reactive.name"));
    }

    @Test
    public void emptyGet() {
        Response response = given().basePath("graphql")
                .contentType("application/json")
                .get();
        Assertions.assertNotEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.statusCode());
        Assertions.assertNotEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.statusCode());
        Assertions.assertNotEquals(HttpStatus.SC_NO_CONTENT, response.statusCode());
        Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    public void singleGet() {
        final Response response = sendGetQuery("friend(name:\"Aristotle\"){name}");
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend.name"));
    }

    @Test
    @Tag("QUARKUS-2485")
    public void singleGetReactive() {
        final Response response = sendGetQuery("friend_reactive(name:\"Aristotle\"){name}");
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend_reactive.name"));
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

    @Test
    @Tag("QUARKUS-2485")
    public void singleGetReactiveWithDefault() {
        final String query = createQuery("friend_reactive_default{name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Aristotle", json.getString("data.friend_reactive_default.name"));
    }

    @Test
    @Tag("QUARKUS-2485")
    public void contextPathReactive() {
        final String query = createQuery("echo_context_path_reactive");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("/echo_context_path_reactive", json.getString("data.echo_context_path_reactive"));
    }

    @Test
    @Tag("QUARKUS-2485")
    public void singleScalar() {
        final String query = createQuery("friend(name:\"Aristotle\"){idol}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Anaxagoras", json.getString("data.friend.idol"));
    }

    @Test
    @Tag("QUARKUS-2485")
    public void map() {
        final String query = createQuery("map(key:PRE_SOCRATIC){value{name}}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Anaxagoras", json.getString("data.map[0].value.name"));
    }

    @Test
    @Tag("QUARKUS-2485")
    public void error() {
        final String query = createQuery("error");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("42", json.getString("errors[0].extensions.code"));
    }
}
