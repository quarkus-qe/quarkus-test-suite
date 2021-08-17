package io.quarkus.qe.scheduling.spring;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class SpringScheduledIT {

    @Test
    public void testAnnotationScheduledCounter() throws InterruptedException {
        Thread.sleep(1000);
        assertCounter("/scheduler/count/annotation", 0);
        Thread.sleep(1000);
        assertCounter("/scheduler/count/annotation", 1);
    }

    private void assertCounter(String counterPath, int expectedCount) {
        String body = given()
                .when().get(counterPath)
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        int actualCounter = Integer.valueOf(body);

        assertTrue(actualCounter > expectedCount,
                "Actual counter '" + actualCounter + "' must be greater than the expected '" + expectedCount + "'");
    }
}
