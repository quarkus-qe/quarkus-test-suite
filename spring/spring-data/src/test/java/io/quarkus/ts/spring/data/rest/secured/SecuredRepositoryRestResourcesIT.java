package io.quarkus.ts.spring.data.rest.secured;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.data.AbstractDbIT;
import io.restassured.http.ContentType;

@Tag("QUARKUS-2788")
@QuarkusScenario
public class SecuredRepositoryRestResourcesIT extends AbstractDbIT {
    private static final long NONEXISTENT_ENTITY_ID = 999;
    private static final String BASE_URL = "/secured";

    private String getUrl(String path) {
        return BASE_URL + path;
    }

    @Test
    void publicResourceNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/public/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void publicResourceDenyAllMethodNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/public"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void publicResourceDenyAllMethodAuth() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("admin", "admin")
                .get(getUrl("/public"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourceNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .delete(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void denyAllResourceAuth() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("admin", "admin")
                .delete(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourcePermitAllMethodNoAuth() {
        given()
                .accept(ContentType.JSON)
                .get(getUrl("/deny-all"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void denyAllResourceRolesAllowedMethodNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void denyAllResourceRolesAllowedMethodAuthForbidden() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("user", "user")
                .get(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourceRolesAllowedMethodAuthPermitted() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("admin", "admin")
                .get(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void permitAllResourceNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/permit-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void permitAllResourceDenyAllMethodNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/permit-all"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void permitAllResourceDenyAllMethodAuth() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("admin", "admin")
                .get(getUrl("/permit-all"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void rolesAllowedResourceNoAuth() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/roles-allowed"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void rolesAllowedResourceAuthForbidden() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("user", "user")
                .get(getUrl("/roles-allowed"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void rolesAllowedResourceAuthPermitted() {
        app.given()
                .accept(ContentType.JSON)
                .auth().basic("admin", "admin")
                .get(getUrl("/roles-allowed"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void rolesAllowedResourcePermitAllMethodWithoutRestResourceAnnotation() {
        app.given()
                .accept(ContentType.JSON)
                .get(getUrl("/roles-allowed/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
