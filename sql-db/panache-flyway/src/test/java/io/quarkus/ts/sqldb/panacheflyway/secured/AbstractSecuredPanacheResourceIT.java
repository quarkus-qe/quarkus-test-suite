package io.quarkus.ts.sqldb.panacheflyway.secured;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.ts.sqldb.panacheflyway.PanacheWithFlywayBaseIT;

public abstract class AbstractSecuredPanacheResourceIT extends PanacheWithFlywayBaseIT {
    private static final long NONEXISTENT_ENTITY_ID = 999;

    protected abstract String getBaseUrl();

    private String getUrl(String path) {
        return getBaseUrl() + path;
    }

    @Test
    void publicResourceNoAuth() {
        given()
                .get(getUrl("/public"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void publicResourceDenyAllMethodNoAuth() {
        given()
                .get(getUrl("/public/count"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void publicResourceDenyAllMethodAuth() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/public/count"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourceNoAuth() {
        given()
                .delete(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void denyAllResourceAuth() {
        given()
                .auth().basic("admin", "admin")
                .delete(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourcePermitAllMethodNoAuth() {
        given()
                .get(getUrl("/deny-all/count"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void denyAllResourceRolesAllowedMethodNoAuth() {
        given()
                .get(getUrl("/deny-all"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void denyAllResourceRolesAllowedMethodAuthForbidden() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/deny-all"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourceRolesAllowedMethodAuthPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/deny-all"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void denyAllResourcePropertiesRolesAllowedMethodNoAuth() {
        given()
                .get(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void denyAllResourcePropertiesRolesAllowedMethodAuthForbidden() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void denyAllResourcePropertiesRolesAllowedMethodAuthPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/deny-all/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void permitAllResourceNoAuth() {
        given()
                .get(getUrl("/permit-all"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void permitAllResourceDenyAllMethodNoAuth() {
        given()
                .get(getUrl("/permit-all/count"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void permitAllResourceDenyAllMethodAuth() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/permit-all/count"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void propertiesRolesAllowedResourceNoAuth() {
        given()
                .get(getUrl("/resource-properties-roles-allowed"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void propertiesRolesAllowedResourceAuthForbidden() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/resource-properties-roles-allowed"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void propertiesRolesAllowedResourceAuthPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/resource-properties-roles-allowed"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void rolesAllowedResourceNoAuth() {
        given()
                .get(getUrl("/roles-allowed"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void rolesAllowedResourceAuthForbidden() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/roles-allowed"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void rolesAllowedResourceAuthPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/roles-allowed"))
                .then().statusCode(HttpStatus.SC_OK);
    }
}
