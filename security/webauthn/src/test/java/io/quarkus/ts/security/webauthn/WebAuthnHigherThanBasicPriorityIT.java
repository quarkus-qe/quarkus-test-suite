package io.quarkus.ts.security.webauthn;

import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.MySqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7342")
@QuarkusScenario
public class WebAuthnHigherThanBasicPriorityIT {
    private static final int MYSQL_PORT = 3306;

    @Container(image = "${mysql.84.image}", port = MYSQL_PORT, expectedLog = "ready for connections.* port: " + MYSQL_PORT)
    static MySqlService database = new MySqlService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("mysql.properties")
            .withProperties("webauthn-basic-priority.properties")
            .withProperty("quarkus.datasource.username", database::getUser)
            .withProperty("quarkus.datasource.password", database::getPassword)
            .withProperty("quarkus.datasource.reactive.url", database::getReactiveUrl)
            .withProperty("quarkus.webauthn.priority", "2")
            .withProperty("quarkus.http.auth.basic.priority", "1");

    @Test
    public void shouldRedirectToWebAuthnLoginWhenPriorityIsHigherThanBasic() {
        app.given()
                .redirects().follow(false)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .header("Location", containsString("/login.html"));
    }
}
