package io.quarkus.ts.http.hibernate.validator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.hibernate.validator.sources.MoviesResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
class ResteasyReactiveMoviesResourceIT {

    @QuarkusApplication(classes = MoviesResource.class, dependencies = {
            @Dependency(artifactId = "quarkus-rest-jackson"),
            @Dependency(artifactId = "quarkus-smallrye-openapi")
    })
    static final RestService app = new RestService();

    @Test
    void testValidationConstraintsOnOpenAPISchemas() throws JsonProcessingException, InterruptedException {
        Response response = given().get("/q/openapi");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode body = mapper.readTree(response.body().asString());

        JsonNode director = body.get("components").get("schemas").get("Director");
        JsonNode movie = body.get("components").get("schemas").get("Movie");

        JsonNode movieId = movie.get("properties").get("id");
        Assertions.assertEquals("\\S", movieId.get("pattern").asText());

        JsonNode directorName = director.get("properties").get("name");
        JsonNode directorSurname = director.get("properties").get("surname");
        Assertions.assertEquals(1, directorName.get("minLength").asInt());
        Assertions.assertEquals(20, directorName.get("maxLength").asInt());
        Assertions.assertEquals("\\S", directorName.get("pattern").asText());
        Assertions.assertEquals(20, directorSurname.get("maxLength").asInt());
        Assertions.assertEquals("\\S", directorSurname.get("pattern").asText());
    }

    @Test
    void getFromMoviesEndpoint() {
        given()
                .get("/movies")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id[0]", is("1"))
                .body("id[1]", is("2"))
                .body("director[1].name", is("Mary"))
                .body("id[2]", is("3"))
                .body("director[2].name", is("Jack"))
                .body("id[3]", is("4"));
    }

    @Test
    void validPostToMoviesEndpoint() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": "10",
                          "director": {
                            "name": "Jim",
                            "surname": "White"
                          },
                          "released": false
                        }
                        """)
                .post("/movies")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void invalidPostToMoviesEndpoint() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": "10",
                          "director": {
                            "name": "",
                            "surname": "White"
                          },
                          "released": false
                        }
                        """)
                .post("/movies")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("Name may not be blank"));
    }
}
