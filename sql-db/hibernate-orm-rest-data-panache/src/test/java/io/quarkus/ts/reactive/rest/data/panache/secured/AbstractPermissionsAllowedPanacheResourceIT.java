package io.quarkus.ts.reactive.rest.data.panache.secured;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;

public abstract class AbstractPermissionsAllowedPanacheResourceIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    public static final PostgresqlService database = new PostgresqlService()
            .withProperty("POSTGRES_DB", "mydb")
            .withProperty("PGDATA", "/tmp/psql");

    @QuarkusApplication
    public static final RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    private static final long NONEXISTENT_ENTITY_ID = 999;

    protected abstract String getBaseUrl();

    private String getUrl(String path) {
        return getBaseUrl() + path;
    }

    // Class-level @PermissionsAllowed("read") — secures all endpoints

    @Test
    void classLevelPermissionsAllowedListNoAuth() {
        given()
                .get(getUrl("/permissions-allowed"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void classLevelPermissionsAllowedListPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/permissions-allowed"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void classLevelPermissionsAllowedListAlsoPermitted() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/permissions-allowed"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void classLevelPermissionsAllowedDeleteNoAuth() {
        given()
                .delete(getUrl("/permissions-allowed/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void classLevelPermissionsAllowedDeletePermitted() {
        given()
                .auth().basic("admin", "admin")
                .delete(getUrl("/permissions-allowed/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void classLevelPermissionsAllowedGetNoAuth() {
        given()
                .get(getUrl("/permissions-allowed/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void classLevelPermissionsAllowedGetPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/permissions-allowed/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void classLevelPermissionsAllowedAddNoAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"test\"}")
                .post(getUrl("/permissions-allowed"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void classLevelPermissionsAllowedUpdateNoAuth() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"test\"}")
                .put(getUrl("/permissions-allowed/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    // Method-level @PermissionsAllowed — only annotated methods are secured

    @Test
    void methodLevelPermissionsAllowedListNoAuth() {
        given()
                .get(getUrl("/permissions-allowed-method"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void methodLevelPermissionsAllowedDeleteNoAuth() {
        given()
                .delete(getUrl("/permissions-allowed-method/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void methodLevelPermissionsAllowedDeleteInvalidAuth() {
        given()
                .auth().basic("admin", "wrong")
                .delete(getUrl("/permissions-allowed-method/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void methodLevelPermissionsAllowedDeleteForbidden() {
        given()
                .auth().basic("admin", "admin")
                .delete(getUrl("/permissions-allowed-method/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void methodLevelPermissionsAllowedDeletePermitted() {
        given()
                .auth().basic("user", "user")
                .delete(getUrl("/permissions-allowed-method/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    // Repeated @PermissionsAllowed — requires ALL permissions (AND logic)

    @Test
    void repeatedPermissionsAllowedCountNoAuth() {
        given()
                .get(getUrl("/permissions-allowed-method/count"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void repeatedPermissionsAllowedCountPartialPermission() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/permissions-allowed-method/count"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void repeatedPermissionsAllowedCountAllPermissions() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/permissions-allowed-method/count"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    // Method-level @PermissionsAllowed overrides class-level @RolesAllowed

    @Test
    void overrideRolesListNoAuth() {
        given()
                .get(getUrl("/roles-permissions-override"))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void overrideRolesListForbidden() {
        given()
                .auth().basic("user", "user")
                .get(getUrl("/roles-permissions-override"))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void overrideRolesListPermitted() {
        given()
                .auth().basic("admin", "admin")
                .get(getUrl("/roles-permissions-override"))
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void overrideRolesDeleteNoAuth() {
        given()
                .delete(getUrl("/roles-permissions-override/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void overrideRolesDeletePermissionOverridesRole() {
        given()
                .auth().basic("admin", "admin")
                .delete(getUrl("/roles-permissions-override/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void overrideRolesDeletePermissionPermitted() {
        given()
                .auth().basic("user", "user")
                .delete(getUrl("/roles-permissions-override/" + NONEXISTENT_ENTITY_ID))
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

}
