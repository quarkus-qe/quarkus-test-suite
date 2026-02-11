package io.quarkus.ts.http.graphql;

import static io.quarkus.ts.http.graphql.utils.GraphQLUtils.createMutation;
import static io.quarkus.ts.http.graphql.utils.GraphQLUtils.createQuery;
import static io.quarkus.ts.http.graphql.utils.GraphQLUtils.sendGetQuery;
import static io.quarkus.ts.http.graphql.utils.GraphQLUtils.sendQuery;
import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public abstract class AbstractGraphQLIT {

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

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void recursive_virtualThread() {
        final String query = createQuery("philosophers_vt{name,friend{name,friend{name}}}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.philosophers_vt[0].name"));
        Assertions.assertEquals("Aristotle", json.getString("data.philosophers_vt[0].friend.name"));
        Assertions.assertEquals("Plato", json.getString("data.philosophers_vt[0].friend.friend.name"));
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void single_virtualThread() {
        final String query = createQuery("friend_vt(name:\"Aristotle\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend_vt.name"));
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void create_virtualThread() {
        final String query = createMutation("create_vt(name:\"Diogen\"){name}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Diogen", json.getString("data.create_vt.name"));
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void singleGet_virtualThread() {
        final Response response = sendGetQuery("friend_vt(name:\"Aristotle\"){name}");
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Plato", json.getString("data.friend_vt.name"));
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void singleScalar_virtualThread() {
        final String query = createQuery("friend_vt(name:\"Aristotle\"){idol}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Anaxagoras", json.getString("data.friend_vt.idol"));
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void map_virtualThread() {
        final String query = createQuery("map_vt(key:PRE_SOCRATIC){value{name}}");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("Anaxagoras", json.getString("data.map_vt[0].value.name"));
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    public void error_virtualThread() {
        final String query = createQuery("error_vt");
        final Response response = sendQuery(query);
        final JsonPath json = response.jsonPath();
        Assertions.assertEquals("42", json.getString("errors[0].extensions.code"));
    }
}
