package io.quarkus.ts.openshift.security.basic;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;

@Tag("QUARKUS-1584")
@QuarkusTest
@TestProfile(SecurityTestProfile.class)
public class SecurityTest {

    private static final String TEST_USER = "testUser";
    private static final String USER_ROLE = "user";
    private static final String ADMIN_ROLE = "admin";

    @Test
    public void permitAll() {
        given().get("/permit-all")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello!"));
    }

    @Test
    public void denyAll() {
        given().get("/deny-all")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void denyUnannotated() {
        given().get("/unannotated")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @TestSecurity(authorizationEnabled = false)
    public void allowUnannotated() {
        given().get("/unannotated")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello!"));
    }

    @Test
    @TestSecurity(user = TEST_USER, roles = USER_ROLE)
    public void allowUserRole() {
        given().get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(TEST_USER));
    }

    @Test
    @TestSecurity(user = TEST_USER, roles = ADMIN_ROLE)
    public void denyAdminRole() {
        given().get("/user")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
