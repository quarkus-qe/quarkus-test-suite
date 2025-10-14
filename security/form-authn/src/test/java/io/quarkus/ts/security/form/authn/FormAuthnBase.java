package io.quarkus.ts.security.form.authn;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.Test;

import io.restassured.filter.cookie.CookieFilter;

public class FormAuthnBase {
    static final String USER_USERNAME = "isaac";
    static final String USER_PASSWORD = "N3wt0N";

    @Test
    public void formAuthN() {
        CookieFilter cookies = new CookieFilter();
        given()
                .filter(cookies)
                .redirects().follow(false)
                .when()
                .get("/user")
                .then()
                .assertThat()
                .statusCode(302)
                .header("location", containsString("/login"))
                .cookie("quarkus-redirect-location", containsString("/user"));
    }

    @Test
    public void formAuthNWrongPassword() {
        CookieFilter cookies = new CookieFilter();
        given()
                .filter(cookies)
                .redirects().follow(false)
                .when()
                .formParam("j_username", USER_USERNAME)
                .formParam("j_password", "wrongpassword")
                .post("/j_security_check")
                .then()
                .assertThat()
                .statusCode(302)
                .header("location", containsString("/error"));
    }

    @Test
    public void testFormBasedAuthSuccessLandingPage() {
        CookieFilter cookies = new CookieFilter();
        given()
                .filter(cookies)
                .redirects().follow(false)
                .when()
                .formParam("j_username", USER_USERNAME)
                .formParam("j_password", USER_PASSWORD)
                .post("/j_security_check")
                .then()
                .assertThat()
                .statusCode(302)
                .header("location", containsString("/landing"))
                .cookie("quarkus-credential", notNullValue());
    }
}
