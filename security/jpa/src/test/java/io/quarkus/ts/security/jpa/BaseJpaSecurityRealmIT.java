package io.quarkus.ts.security.jpa;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

public abstract class BaseJpaSecurityRealmIT {

    @Test
    void shouldAccessPublicWhenAnonymous() {
        get("/api/public")
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    void shouldNotAccessAdminWhenAnonymous() {
        get("/api/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

    }

    @Test
    void shouldAccessAdminWhenAdminAuthenticated() {
        given()
                .auth().preemptive().basic("admin", "admin")
                .when()
                .get("/api/admin")
                .then()
                .statusCode(HttpStatus.SC_OK);

    }

    @Test
    void shouldNotAccessUserWhenAdminAuthenticated() {
        given()
                .auth().preemptive().basic("admin", "admin")
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void shouldAccessUserAndGetIdentityWhenUserAuthenticated() {
        given()
                .auth().preemptive().basic("user", "user")
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("user"));
    }

    @Test
    void createUserThatShouldNotAccessAdmin() {
        String requestBody = "{\"username\": \"newUser\", \"password\": \"user\", \"role\": \"user\"}";

        given().contentType(ContentType.JSON).body(requestBody).post("/create/user").then()
                .statusCode(HttpStatus.SC_CREATED);

        given()
                .auth().preemptive().basic("newUser", "user")
                .when()
                .get("/api/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
