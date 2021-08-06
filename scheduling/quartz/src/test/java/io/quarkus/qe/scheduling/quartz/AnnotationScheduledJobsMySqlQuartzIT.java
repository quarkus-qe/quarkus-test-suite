package io.quarkus.qe.scheduling.quartz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.scheduling.quartz.failover.AnnotationScheduledJob;
import io.quarkus.qe.scheduling.quartz.failover.ExecutionEntity;
import io.quarkus.qe.scheduling.quartz.failover.ExecutionService;
import io.quarkus.qe.scheduling.quartz.failover.ExecutionsResource;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnNative(reason = "Due to high native build execution time for the three Quarkus services")
public class AnnotationScheduledJobsMySqlQuartzIT extends BaseMySqlQuartzIT {

    static final String NODE_ONE_NAME = "node-one";
    static final String NODE_TWO_NAME = "node-two";

    @QuarkusApplication(classes = { AnnotationScheduledJob.class, ExecutionEntity.class, ExecutionService.class })
    static RestService one = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("owner.name", NODE_ONE_NAME)
            .withProperty("quarkus.datasource.jdbc.url", BaseMySqlQuartzIT::mysqlJdbcUrl);

    @QuarkusApplication(classes = { AnnotationScheduledJob.class, ExecutionEntity.class, ExecutionService.class })
    static RestService two = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("owner.name", NODE_TWO_NAME)
            .withProperty("quarkus.datasource.jdbc.url", BaseMySqlQuartzIT::mysqlJdbcUrl);

    @QuarkusApplication(classes = { ExecutionsResource.class, ExecutionEntity.class })
    static RestService app = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("quarkus.datasource.jdbc.url", BaseMySqlQuartzIT::mysqlJdbcUrl);

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
