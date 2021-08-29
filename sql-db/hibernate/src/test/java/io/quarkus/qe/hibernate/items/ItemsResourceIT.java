package io.quarkus.qe.hibernate.items;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class ItemsResourceIT {

    @DevModeQuarkusApplication
    //TODO: simplify when https://github.com/quarkus-qe/quarkus-test-framework/issues/249 is resolved
    static RestService app = new RestService().onPostStart(app -> {
        app.logs().assertContains("Listening on");
    });

    /**
     * Required data is pulled in from the `import.sql` resource.
     */
    @Test
    public void shouldNotFailWithConstraints() throws InterruptedException {
        Thread.sleep(5000);
        given().when().get("/items/count").then().body(is("1"));
    }
}
