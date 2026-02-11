package io.quarkus.ts.http.graphql.telemetry.utils;

import static io.restassured.RestAssured.given;

import jakarta.json.Json;

import io.restassured.response.Response;

public class GraphQLUtils {
    public static Response sendQuery(String query) {
        return given().basePath("graphql")
                .contentType("application/json")
                .body(query)
                .post();
    }

    public static String createQuery(String query) {
        return Json.createObjectBuilder()
                .add("query", "{" + query + "}")
                .build().toString();
    }
}
