package io.quarkus.ts.security.permissions.beanparam;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.Matchers.containsString;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class BeanParamPermissionIT extends BaseBeanParamPermissionsIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Override
    protected RequestSpecification givenAuthenticatedUser(String role) {
        return app.given().auth().basic(role, role);
    }

    /**
     * Verify that a user with the correct role can access resources protected with permissions using @BeanParam fields.
     */
    @Test
    public void testBasicAuthWithBeanParam() {
        givenAuthenticatedUser("INVALID")
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "res-123")
                .queryParam("action", "basic")
                .when()
                .get(SIMPLE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "res-123")
                .queryParam("action", "basic")
                .when()
                .get(SIMPLE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Simple access granted to resource res-123"));
    }

    @Test
    public void testAdminCanAccessWithGenericReadAction() {
        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "res-123")
                .queryParam("action", "read")
                .when()
                .get(SIMPLE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Simple access granted to resource res-123"));

        givenAuthenticatedUser(USER)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "res-123")
                .queryParam("action", "read:basic")
                .when()
                .get(SIMPLE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    @Test
    public void testWritePermissionForAdmin() {
        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "doc-123")
                .queryParam("action", "write")
                .body("Test content for writing")
                .contentType(MediaType.TEXT_PLAIN)
                .when()
                .post("/bean-param/write")
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Write successful to resource"));
    }

    @Test
    public void testWritePermissionForUser() {
        givenAuthenticatedUser(USER)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "doc-123")
                .queryParam("action", "write")
                .body("Test content for writing")
                .contentType(MediaType.TEXT_PLAIN)
                .when()
                .post("/bean-param/write")
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    @Test
    public void testWritePermissionWithInvalidAction() {
        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "doc-123")
                .queryParam("action", "invalid-action")
                .body("Test content for writing")
                .contentType(MediaType.TEXT_PLAIN)
                .when()
                .post("/bean-param/write")
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    @Test
    public void testWriteAccessDeniedForUser() {
        givenAuthenticatedUser(USER)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "res-123")
                .queryParam("action", "write")
                .body("Test content for writing")
                .contentType(MediaType.TEXT_PLAIN)
                .when()
                .post("/bean-param/write")
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    /**
     * Both tests testDeniedWhenBeanParamValuesDontSatisfyRequirements and testRecordBeanParamDenialInvalidDocumentId verify
     * that access is denied
     * when @BeanParam field values don't satisfy permission requirements.
     */
    @Test
    public void testDeniedWhenBeanParamValuesDontSatisfyRequirements() {
        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("resourceId", "res-123")
                .queryParam("action", "invalid-action")
                .when()
                .get(SIMPLE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    @Test
    public void testRecordBeanParamDenialInvalidDocumentId() {
        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("docId", "invalid-id")
                .queryParam("version", "1")
                .queryParam("accessLevel", "read")
                .when()
                .get(RECORD_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    @Test
    public void testRecordBeanParamSuccess() {
        givenAuthenticatedUser(ADMIN)
                .header("CustomAuthorization", "valid-token")
                .queryParam("docId", "doc-123")
                .queryParam("version", "1.0")
                .queryParam("accessLevel", "read")
                .when()
                .get(RECORD_ENDPOINT)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Record access granted to document doc-123"));
    }

    /**
     * Missing @BeanParam values (in this case header) got 403
     */
    @Test
    public void testCommonFieldBeanParamDenialMissingHeader() {
        givenAuthenticatedUser(ADMIN)
                .queryParam("operation", "read")
                .when()
                .get(COMMON_FIELD_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }

    /**
     * Verify support for referencing nested properties in permission checks
     */

    @Test
    public void testNestedProfileAccessSuccess() {
        givenAuthenticatedUser(USER)
                .queryParam("userId", "someUserId")
                .queryParam("userName", "Alice")
                .queryParam("resourceId", "someUserId")
                .queryParam("resourceType", "profile")
                .when()
                .get(NESTED_PROFILE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Access granted to profile. User: Alice (ID: someUserId), Profile ID: someUserId"));
    }

    /**
     * Test nested properties with null or missing values.
     */
    @Test
    public void testNestedPropertiesWithMissingValues() {
        givenAuthenticatedUser(ADMIN)
                .queryParam("userId", "user123")
                .queryParam("resourceId", "doc-abc")
                .queryParam("resourceType", "document")
                .when()
                .get(NESTED_DOCUMENT_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);

        givenAuthenticatedUser(USER)
                .queryParam("userId", "someUserId")
                .queryParam("userName", "Alice")
                .queryParam("resourceId", "differentId")
                .queryParam("resourceType", "profile")
                .when()
                .get(NESTED_PROFILE_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .statusCode(SC_FORBIDDEN);
    }
}
