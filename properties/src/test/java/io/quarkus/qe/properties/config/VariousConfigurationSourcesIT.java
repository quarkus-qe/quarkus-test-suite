package io.quarkus.qe.properties.config;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class VariousConfigurationSourcesIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperty("welcome.message", "Welcome message from system property");

    @Test
    public void testEnvFile() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Welcome message from system property"));
    }

    @Test
    public void testYamlPropertiesByInterface() {
        // Property 'protagonist.hobby' overridden by ./configsource.prop file that has higher ordinal(priority)
        given()
                .when().get("/hello/protagonist")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Sponge Bob says: Hi, I am Sponge Bob. My hobie is: Jellyfishing"));
    }

    @Test
    public void testYamlPropertiesByInterfaceNested() {
        given()
                .when().get("/hello/protagonist/friend")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Patrick Star says: Hi, I am Patrick Star"));
    }

    @Test
    public void testApplicationPropertiesByClass() {
        given()
                .when().get("/hello/antagonist")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Sheldon Plankton says: Hi, I am Sheldon Plankton"));
    }

    @Test
    public void testApplicationPropertiesByClassNested() {
        given()
                .when().get("/hello/antagonist/wife")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Karen says: Hi, I am Karen"));
    }

    @Test
    public void testCustomEmailConverter() {
        // File ./application.yaml takes precedence over ./application.properties
        given()
                .when().get("/hello/emails")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        containsString("spongebob@krustykrab.com"),
                        containsString("squidwardtentacles@krustykrab.com"),
                        containsString("eugenekrabs@krustykrab.com"));
    }
}
