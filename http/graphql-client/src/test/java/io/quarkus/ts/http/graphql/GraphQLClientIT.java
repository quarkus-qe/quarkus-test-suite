package io.quarkus.ts.http.graphql;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
@Tag("QUARKUS-6521")
@QuarkusScenario
public class GraphQLClientIT {

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-smallrye-graphql"))
    static final RestService app = new RestService();

    @Test
    public void testClientCalledFromVirtualThread() {
        app
                .given()
                .contentType("application/json")
                .get("/graphql?query=" + URLEncoder.encode("{date_vt}", UTF_8))
                .then()
                .statusCode(200)
                .body("data.date_vt", Matchers.is("2025-03-13T11:47:13+01:00"));
    }
}
