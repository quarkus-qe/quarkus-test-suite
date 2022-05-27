package io.quarkus.ts.scheduling.quartz;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class BasicMySqlQuartzIT extends BaseMySqlQuartzIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    @Test
    public void testAnnotationScheduledCounter() throws InterruptedException {
        Thread.sleep(1000);
        assertCounter("/scheduler/count/annotation", 0);
        Thread.sleep(1000);
        assertCounter("/scheduler/count/annotation", 1);
    }

    @Test
    public void testManuallyScheduledCounter() throws InterruptedException {
        Thread.sleep(1000);
        assertCounter("/scheduler/count/manual", 0);
        Thread.sleep(1000);
        assertCounter("/scheduler/count/manual", 1);
    }

    private void assertCounter(String counterPath, int expectedCount) {
        String body = app.given()
                .when().get(counterPath)
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        int actualCounter = Integer.valueOf(body);

        assertTrue(actualCounter > expectedCount,
                "Actual counter '" + actualCounter + "' must be greater than the expected '" + expectedCount + "'");
    }
}
