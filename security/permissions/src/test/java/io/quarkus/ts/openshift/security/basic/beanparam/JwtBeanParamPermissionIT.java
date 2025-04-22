package io.quarkus.ts.openshift.security.basic.beanparam;

import static org.hamcrest.Matchers.containsString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;

@QuarkusScenario
public class JwtBeanParamPermissionIT extends BaseBeanParamPermissionsIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.security.deny-unannotated-members", "true");

    @Override
    protected RequestSpecification givenAuthenticatedUser(String role) {
        String token = generateToken(role, role);
        return app.given().header("Authorization", "Bearer " + token);
    }

    @Test
    public void testSimpleBeanParamWithJwtAdmin() {
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

    public static String generateToken(String username, String... roles) {
        Set<String> roleSet = new HashSet<>(Arrays.asList(roles));
        return Jwt.issuer("https://my.auth.server/")
                .upn(username)
                .subject(username)
                .groups(roleSet)
                .expiresAt(System.currentTimeMillis() + 3600 * 1000)
                .sign();
    }
}
