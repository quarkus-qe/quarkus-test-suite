package io.quarkus.qe.scheduling.quartz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.scheduling.quartz.failover.ExecutionEntity;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class AnnotationScheduledJobsMySqlQuartzIT extends BaseMySqlQuartzIT {

    static final String NODE_ONE_NAME = "node-one";
    static final String NODE_TWO_NAME = "node-two";

    @QuarkusApplication
    static RestService one = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("owner.name", NODE_ONE_NAME)
            .withProperty("quarkus.datasource.jdbc.url", BaseMySqlQuartzIT::mysqlJdbcUrl);

    @QuarkusApplication
    static RestService two = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("owner.name", NODE_TWO_NAME)
            .withProperty("quarkus.datasource.jdbc.url", BaseMySqlQuartzIT::mysqlJdbcUrl);

    @QuarkusApplication
    static RestService app = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("quarkus.datasource.jdbc.url", BaseMySqlQuartzIT::mysqlJdbcUrl)
            // Disable scheduler, so this app behaves as viewer of the two nodes.
            .withProperty("quarkus.scheduler.enabled", "false");

    @Test
    public void testClusteringEnvironmentWithUniqueJobs() {
        thenNodeJobIsExecutedWithOwner(NODE_ONE_NAME);

        whenShutdownNodeOne();
        thenNodeJobIsExecutedWithOwner(NODE_TWO_NAME);
    }

    private void whenShutdownNodeOne() {
        one.stop();
    }

    private void thenNodeJobIsExecutedWithOwner(String expectedOwner) {
        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            ExecutionEntity[] executions = app.given().get("/executions")
                    .then().statusCode(HttpStatus.SC_OK)
                    .extract().as(ExecutionEntity[].class);

            assertEquals(expectedOwner, executions[executions.length - 1].owner, "Expected owner not found");
        });
    }
}
