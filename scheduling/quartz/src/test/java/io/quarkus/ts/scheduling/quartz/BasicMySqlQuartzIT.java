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
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus-qe.enable-manually-scheduled-counter", "true");

    @Test
    public void testAnnotationScheduledCounter() throws InterruptedException {
        Thread.sleep(1000);
        assertCounter("/scheduler/count/annotation", 0);
        Thread.sleep(1000);
        assertCounter("/scheduler/count/annotation", 1);
    }

    @Test
    public void testManuallyScheduledCounter() throws InterruptedException {
        // we want to take first count result and test that after a second
        // count is higher; manually scheduled job starts way before this test
        // unless it is started as only test, therefore we can't have fixed numbers
        int firstCount = assertCounter("/scheduler/count/manual", 0);
        Thread.sleep(1000);
        assertCounter("/scheduler/count/manual", firstCount);
    }

    private int assertCounter(String counterPath, int expectedCount) {
        String body = app.given()
                .when().get(counterPath)
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        int actualCounter = Integer.parseInt(body);

        assertTrue(actualCounter > expectedCount,
                "Actual counter '" + actualCounter + "' must be greater than the expected '" + expectedCount + "'");

        return actualCounter;
    }
}
