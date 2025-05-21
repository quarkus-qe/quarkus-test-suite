package io.quarkus.qe.hibernate;

import static io.quarkus.qe.hibernate.analyze.AnalyzeResource.AUTHOR;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;

@Tag("QUARKUS-3731")
public abstract class BaseHibernateIT {

    private static final String TRANSACTION_SCOPE_BASE_PATH = "/transaction-scope";

    private static final String EXPECTED_RESPONSE_FROM_INVOKE_BEAN = "1";
    private static final String TRUE = Boolean.TRUE.toString();
    private static final String FALSE = Boolean.FALSE.toString();

    protected abstract RestService getApp();

    /**
     * Required data is pulled in from the `import.sql` resource.
     */
    @Test
    public void shouldNotFailWithConstraints() {
        given().when().get("/items/count").then().body(is("1"));
    }

    @Tag("https://github.com/quarkusio/quarkus/pull/46940")
    @Test
    public void testJsonEntityMappingWithJsonbUnremovable() {
        String jsonPayload = "{\"testJsonEntity\": {\"field1\": \"value1\", \"field2\": 42}}";

        String idString = getApp().given()
                .contentType(MediaType.TEXT_PLAIN)
                .body(jsonPayload)
                .post("/json-entity/create/Test Entity")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        Long id = Long.parseLong(idString);

        String retrievedJson = getApp().given()
                .pathParam("id", id)
                .get("/json-entity/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        assertEquals(jsonPayload, retrievedJson);
        /*
         * The warning was present before the fix: "CDI: programmatic lookup problem detected
         * At least one bean matched the required type and qualifiers but was marked as unused and removed during build"
         */
        String cdiProblemDetected = "CDI: programmatic lookup problem detected";
        String markedAsRemovedLog = "marked as unused and removed during build";
        String removedBeansHeaderLog = "Removed beans:";

        getApp().logs().assertDoesNotContain(cdiProblemDetected);
        getApp().logs().assertDoesNotContain(markedAsRemovedLog);
        getApp().logs().assertDoesNotContain(removedBeansHeaderLog);

    }

    @Test
    public void shouldPostConstructAndPreDestroyBeInvoked() {
        givenPostConstructAndPreDestroyAreNotInvoked();
        whenInvokeBean();
        thenIsPostConstructInvoked();
        thenIsPreDestroyInvoked();
    }

    @Test
    public void testJakartaPersistenceAndHibernateValidatorEndpoint() {
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

    @Test
    public void useReservedWordAsTableName() {
        // verifies https://github.com/quarkusio/quarkus/issues/28593
        var id = given()
                .post("/analyze")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(notNullValue())
                .extract()
                .asString();

        given()
                .pathParam("id", id)
                .get("/analyze/{id}/author")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.is(AUTHOR));
    }

    @Test
    @Tag("https://issues.redhat.com/browse/QUARKUS-5714")
    public void testCreationAndUpdateInOneTransaction() {
        long id = 10;
        String initData = "Init data";
        String updateData = "Updated data";
        given()
                .queryParam("id", id)
                .queryParam("initData", initData)
                .queryParam("updateData", updateData)
                .post("/entity-creation/create-and-update")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        given()
                .get("/entity-creation/" + id)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(updateData));
    }

    private void givenPostConstructAndPreDestroyAreNotInvoked() {
        assertEquals(FALSE, getPostConstructInvokeResult(), "PostConstruct method has been invoked already");
        assertEquals(FALSE, getPreDestroyInvokeResult(), "PreDestroy method has been invoked already");
    }

    private void whenInvokeBean() {
        given().when().post(transactionScopePath("/invoke-bean")).then().body(is(EXPECTED_RESPONSE_FROM_INVOKE_BEAN));
    }

    private void thenIsPostConstructInvoked() {
        assertEquals(TRUE, getPostConstructInvokeResult(), "PostConstruct method is not invoked");
    }

    private void thenIsPreDestroyInvoked() {
        assertEquals(TRUE, getPreDestroyInvokeResult(), "PreDestroy method is not invoked");
    }

    private String getPostConstructInvokeResult() {
        return given().when().get(transactionScopePath("/is-post-construct-invoked")).asString();
    }

    private String getPreDestroyInvokeResult() {
        return given().when().get(transactionScopePath("/is-pre-destroy-invoked")).asString();
    }

    private String transactionScopePath(String path) {
        return TRANSACTION_SCOPE_BASE_PATH + path;
    }
}
