package io.quarkus.ts.security.bouncycastle.fips;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class BouncyCastleFipsJsseIT {

    private static final String PASSWORD = "password";

    @QuarkusApplication(dependencies = {
            @Dependency(groupId = "org.bouncycastle", artifactId = "bctls-fips", version = "${bouncycastle.bctls-fips.version}")
    })
    private static final RestService app = new RestService().withProperties("jsse.properties");

    @Test
    public void verifyBouncyCastleFipsAndJsseProviderAvailability() {
        given()
                .keyStore(Paths.get("client-keystore.jks").toFile(), PASSWORD)
                .trustStore(Paths.get("client-truststore.jks").toFile(), PASSWORD)
                .when()
                .get("/api/listProviders")
                .then()
                .statusCode(200)
                .body(containsString("BCFIPS,BCJSSE"));
    }
}
