package io.quarkus.ts.scheduling.quartz;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class ScheduledTaskSecurityIT extends BaseMySqlQuartzIT {

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-security"))
    static RestService app = new RestService().withProperties(MYSQL_PROPERTIES)
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    void authenticatedMethodIsDeniedWithoutIdentity() {
        assertScheduledExecutionObserved(
                "/scheduled-security/authenticated/failure",
                "Expected @Authenticated method to be denied when called from @Scheduled task without identity");
    }

    @Test
    void authenticatedMethodIsAllowedWithRunAsUser() {
        assertScheduledExecutionObserved(
                "/scheduled-security/authenticated/success",
                "Expected @Authenticated method to succeed when called from @Scheduled task with @RunAsUser");
    }

    @Test
    void adminMethodIsAllowedWithCorrectRole() {
        assertScheduledExecutionObserved(
                "/scheduled-security/admin/success",
                "Expected @RolesAllowed(admin) method to succeed when correct 'admin' role is assigned");
    }

    @Test
    void adminMethodIsDeniedWithoutIdentity() {
        assertScheduledExecutionObserved(
                "/scheduled-security/admin/failure/no-identity",
                "Expected @RolesAllowed(admin) method to be denied without identity");
    }

    @Test
    void adminMethodIsDeniedWithWrongRole() {
        assertScheduledExecutionObserved(
                "/scheduled-security/admin/failure/wrong-role",
                "Expected @RolesAllowed(admin) method to be denied when wrong 'user' role is assigned");
    }

    @Test
    void permitAllMethodIsAccessibleWithoutIdentity() {
        assertScheduledExecutionObserved(
                "/scheduled-security/permit-all/success",
                "Expected @PermitAll method to be accessible from @Scheduled task without identity");
    }

    @Test
    void failureInOneScheduledTaskDoesNotAffectOthers() {
        await().atMost(TIMEOUT).untilAsserted(() -> {

            int adminFailures = getCounterValue("/scheduled-security/admin/failure/wrong-role");
            int authenticatedSuccess = getCounterValue("/scheduled-security/authenticated/success");

            assertTrue(
                    adminFailures > 0,
                    "Expected admin scheduled task with wrong role to fail, but failure counter = "
                            + adminFailures);

            assertTrue(
                    authenticatedSuccess > 0,
                    "Expected authenticated scheduled task to continue running despite failures in another task, but success counter = "
                            + authenticatedSuccess);
        });
    }

    private void assertScheduledExecutionObserved(String path, String failureMessage) {
        await().atMost(TIMEOUT).untilAsserted(() -> {
            int value = getCounterValue(path);
            assertTrue(
                    value > 0,
                    failureMessage + ", actual counter = " + value);
        });
    }

    private int getCounterValue(String path) {
        return Integer.parseInt(
                app.given()
                        .get(path)
                        .then()
                        .statusCode(200)
                        .extract()
                        .asString());
    }
}
