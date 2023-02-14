package io.quarkus.ts.http.graphql;

import static io.restassured.RestAssured.given;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.json.Json;

import io.restassured.response.Response;

public class Utils {
    public static Response sendQuery(String query) {
        return given().basePath("graphql")
                .contentType("application/json")
                .body(query)
                .post();
    }

    public static Response sendGetQuery(String query) {
        return given()
                .contentType("application/json")
                .get("/graphql?query=" + URLEncoder.encode("{" + query + "}", StandardCharsets.UTF_8));
    }

    public static String createQuery(String query) {
        return Json.createObjectBuilder()
                .add("query", "{" + query + "}")
                .build().toString();
    }

    public static String createMutation(String query) {
        return Json.createObjectBuilder()
                .add("query", "mutation {" + query + "}")
                .build().toString();
    }
}
