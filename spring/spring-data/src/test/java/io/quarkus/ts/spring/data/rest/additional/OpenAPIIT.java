package io.quarkus.ts.spring.data.rest.additional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.data.AbstractDbIT;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@Tag("QUARKUS-2788")
@QuarkusScenario
public class OpenAPIIT extends AbstractDbIT {
    @Test
    void rolesAllowedResourceAuthPermitted() {
        Response response = app.given()
                .accept(ContentType.JSON)
                .get("/q/openapi");
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .header("Content-Type", "application/json;charset=UTF-8");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        JsonPath json = response.getBody().jsonPath();
        assertEquals("spring-data API", json.getString("info.title"));

        assertNotNull(json.getString("paths./article-jpa.get"));

        json.setRootPath("paths./article-jpa.get");
        assertEquals("namedQuery", json.getString("parameters[0].name"));
        assertEquals("string", json.getString("parameters[0].schema.type"));
        assertEquals("page", json.getString("parameters[1].name"));
        assertEquals("integer", json.getString("parameters[1].schema.type"));
        assertEquals("size", json.getString("parameters[2].name"));
        assertEquals("integer", json.getString("parameters[2].schema.type"));
        assertEquals("sort", json.getString("parameters[3].name"));
        assertEquals("array", json.getString("parameters[3].schema.type"));

        assertEquals(4, json.getList("parameters").size());

        assertEquals("#/components/schemas/Article",
                json.getString("responses.200.content.\"application/json\".schema.items.$ref"));

        json.setRootPath("").setRootPath("paths./article-jpa.post");
        assertEquals("#/components/schemas/Article",
                json.getString("requestBody.content.\"application/json\".schema.$ref"));
        assertEquals("#/components/schemas/Article",
                json.getString("responses.201.content.\"application/json\".schema.$ref"));
        json.setRootPath("");
        assertNotNull(json.getString("paths./article-jpa.get"));
        assertNotNull(json.getString("paths.\"/article-jpa/{id}\".get"));

        json.setRootPath("");

        // OpenAPI should generate per-scope security requirements only for OAuth2
        // more info: https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#patterned-fields-3
        assertEquals("admin", json.getString("paths.\"/secured/deny-all/{id}\".get.security[0].SecurityScheme[0]"));
        assertEquals("admin", json.getString("paths./secured/roles-allowed.get.security[0].SecurityScheme[0]"));
        assertEquals("admin", json.getString("paths.\"/secured/roles-allowed/{id}\".get.security[0].SecurityScheme[0]"));
        assertEquals("user", json.getString("paths.\"/secured/roles-allowed/{id}\".delete.security[0].SecurityScheme[0]"));

        List<String> list = json.getList("components.schemas.Article.required");
        assertEquals(2, list.size());
        assertThat(list, contains("name", "author"));

        json.setRootPath("").setRootPath("components.schemas.Article.properties");
        assertEquals("int64", json.getString("id.format"));
        assertEquals("string", json.getString("name.type"));
        assertEquals("string", json.getString("author.type"));
        assertEquals("#/components/schemas/Library", json.getString("library.$ref"));

        json.setRootPath("");
        assertEquals("2022-03-10T12:15:50", json.getString("components.schemas.LocalDateTime.examples[0]"));

        assertEquals("http", json.getString("components.securitySchemes.SecurityScheme.type"));
        assertEquals("Authentication", json.getString("components.securitySchemes.SecurityScheme.description"));
        assertEquals("basic", json.getString("components.securitySchemes.SecurityScheme.scheme"));

    }
}
