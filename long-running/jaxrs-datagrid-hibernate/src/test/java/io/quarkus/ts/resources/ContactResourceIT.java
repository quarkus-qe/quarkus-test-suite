package io.quarkus.ts.resources;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.memoryLeaks.MemoryManager;
import io.quarkus.ts.memoryLeaks.MemoryReport;
import io.quarkus.ts.model.Contact;
import io.restassured.response.ValidatableResponse;

@QuarkusScenario
@TestInstance(Lifecycle.PER_CLASS)
public class ContactResourceIT {

    static final Logger LOG = Logger.getLogger(ContactResourceIT.class.getName());
    LongRunningUtils longRunning = new LongRunningUtils();
    MemoryManager leaksHelper = new MemoryManager();

    @BeforeAll
    public void tearUp() {
        given().log().ifValidationFails().when()
                .body(defaultContact())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post("/contacts")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @AfterAll
    public void tearDown() {
        given().when()
                .delete("/contacts")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void longRunningRetrieveById() throws InterruptedException {
        // WarmingUp
        leaksHelper.warmingUp("/contacts/1");

        // Business logic
        await()
                .atMost(longRunning.getTimeout())
                .with()
                .pollInterval(longRunning.getPollInterval())
                .until(() -> {
                    Contact contact = getContactQuery(1).extract().as(Contact.class);
                    assertThat(contact.getName(), is(defaultContact().getName()));
                    leaksHelper.pullJvmMemoryUsage();
                    return longRunning.isCompleted();
                });

        // Garbage collector
        leaksHelper.runGC();

        // Cool down
        LOG.info("Cooling down...");
        await()
                .atMost(longRunning.getCoolDown())
                .with()
                .pollInterval(longRunning.getPollInterval())
                .until(() -> {
                    leaksHelper.pullCoolDownJvmMemoryUsage();
                    return longRunning.isCoolDownCompleted();
                });

        MemoryReport report = leaksHelper.memoryLeaksReport();
        LOG.info(report.reportPretty());

        assertThat("MinMemory usage is greater than expected", !report.isMemoryLeaks());
    }

    private ValidatableResponse getContactQuery(int id) {
        return given()
                .when().get("/contacts/" + id)
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private Contact defaultContact() {
        Contact contact = new Contact();
        contact.setName("Paul");
        contact.setPhoneNumber("0034608455678");
        contact.setSavedBy("Hasapi Papadopoulos");

        return contact;
    }
}
