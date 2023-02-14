package io.quarkus.ts.http.graphql.telemetry;

import static io.restassured.RestAssured.given;

import jakarta.json.Json;

import io.restassured.response.Response;

public class Utils {
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
