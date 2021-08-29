package io.quarkus.qe.hibernate.validator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class JpaAndHibernateValidatorResourceIT {

    @DevModeQuarkusApplication
    //TODO: simplify when https://github.com/quarkus-qe/quarkus-test-framework/issues/249 is resolved
    static RestService app = new RestService().onPostStart(app -> {
        app.logs().assertContains("Listening on");
    });

    @Test
    public void testJpaAndHibernateValidatorEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .body(containsString("hello"))
                .body(not(containsString("HV000041")));

        // second request is where the issue appears
        given()
                .when().get("/hello")
                .then()
                .body(containsString("hello"))
                .body(not(containsString("HV000041")))
                .body(not(containsString("HV000")));
    }

}
