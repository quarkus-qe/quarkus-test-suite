package io.quarkus.qe.hibernate;

import static io.quarkus.qe.hibernate.analyze.AnalyzeResource.AUTHOR;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.hibernate.data.TestDataEntity;
import io.quarkus.qe.hibernate.flush.FlushEventListener;
import io.quarkus.qe.hibernate.interceptor.SessionEventInterceptor;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

@Tag("QUARKUS-6261")
@Tag("QUARKUS-3731")
@Tag("QUARKUS-7171")
public abstract class BaseHibernateIT {

    private static final String TRANSACTION_SCOPE_BASE_PATH = "/transaction-scope";

    private static final String EXPECTED_RESPONSE_FROM_INVOKE_BEAN = "1";
    private static final String TRUE = Boolean.TRUE.toString();
    private static final String FALSE = Boolean.FALSE.toString();

    @LookupService
    static RestService app;

    /**
     * Required data is pulled in from the `import.sql` resource.
     */
    @Test
    public void shouldNotFailWithConstraints() {
        app.given().when().get("/items/count").then().body(is("1"));
    }

    @Tag("https://github.com/quarkusio/quarkus/pull/46940")
    @Test
    public void testJsonEntityMappingWithJsonbUnremovable() {
        String jsonPayload = "{\"testJsonEntity\": {\"field1\": \"value1\", \"field2\": 42}}";

        String idString = app.given()
                .contentType(MediaType.TEXT_PLAIN)
                .body(jsonPayload)
                .post("/json-entity/create/Test Entity")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();

        Long id = Long.parseLong(idString);

        String retrievedJson = app.given()
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

        app.logs().assertDoesNotContain(cdiProblemDetected);
        app.logs().assertDoesNotContain(markedAsRemovedLog);
        app.logs().assertDoesNotContain(removedBeansHeaderLog);

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
        app.given()
                .when().get("/hello")
                .then()
                .body(containsString("hello"))
                .body(not(containsString("HV000041")));

        // second request is where the issue appears
        app.given()
                .when().get("/hello")
                .then()
                .body(containsString("hello"))
                .body(not(containsString("HV000041")))
                .body(not(containsString("HV000")));
    }

    @Test
    public void useReservedWordAsTableName() {
        // verifies https://github.com/quarkusio/quarkus/issues/28593
        var id = app.given()
                .post("/analyze")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(notNullValue())
                .extract()
                .asString();

        app.given()
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
        app.given()
                .queryParam("id", id)
                .queryParam("initData", initData)
                .queryParam("updateData", updateData)
                .post("/entity-creation/create-and-update")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        app.given()
                .get("/entity-creation/" + id)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(updateData));
    }

    @Test
    void testSessionMethods() {
        // tests migration between Session methods as mentioned in the migration guide
        // method: persist
        app.given()
                .body("Some data")
                .post("/session/persist/5002")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // method: find
        app.given()
                .get("/session/find/5002")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Some data"));
        // method: remove & getReference
        app.given()
                .get("/session/get-reference/5002")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Some data"));
        app.given()
                .delete("/session/remove/5002")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("true"));
        app.given()
                .get("/session/get-reference/5002")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void testTemporalTypesReturnedFromNativeQuery() {
        // temporal types in native query results should be mapped to 'java.time'
        app.given()
                .queryParam("customerId", Integer.toString(1))
                .get("/native-query/temporal-types")
                .then().statusCode(200)
                .body(is(LocalDateTime.now().getYear() + ""));
    }

    @Test
    void testMappingBasicJavaTypeArraysToDatabaseArrays() {
        app.given()
                .queryParam("customerId", Integer.toString(1))
                .get("/native-query/basic-array-mapping")
                .then().statusCode(200)
                .body(is("MIT - GPL"));
    }

    @Test
    void test2ndLevelCacheForStatelessSession() {
        int idAsInt = RandomGenerator.getDefault().nextInt();
        String id = Integer.toString(idAsInt);
        // create
        app.given()
                .pathParam("id", id)
                .body("Le boudin")
                .post("/stateless-session/insert/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // get
        app.given()
                .pathParam("id", id)
                .get("/stateless-session/get/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Le boudin"));
        // update
        app.given()
                .pathParam("id", id)
                .body("Black Lung")
                .put("/stateless-session/update/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // get
        app.given()
                .pathParam("id", id)
                .get("/stateless-session/get/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Black Lung"));
        // assert secondary cache contains
        app.given()
                .pathParam("id", id)
                .get("/stateless-session/cache/contains/{id}")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("true"));
        // update the database table using JDBC connection directly, so that Hibernate is not aware of the change
        app.given()
                .put("/stateless-session/update-using-connection-directly")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // get - expect that native query update had no effect
        app.given()
                .pathParam("id", id)
                .get("/stateless-session/get/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Black Lung"));
        // evict cache
        app.given()
                .delete("/stateless-session/cache/evict-all")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // now expect to see the updated row
        app.given()
                .pathParam("id", id)
                .get("/stateless-session/get/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Black Lung".repeat(2)));
        // clean up
        app.given()
                .pathParam("id", id)
                .delete("/stateless-session/delete/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given()
                .pathParam("id", id)
                .get("/stateless-session/get/{id}")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void testSchemaManager() {
        // data comes from the 'import.sql' script, which means we test that the script is re-applied
        app.given().when().get("/items/count").then().statusCode(HttpStatus.SC_OK).body(is("1"));
        app.given().delete("/schema/drop").then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given().when().get("/items/count").then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        app.given().post("/schema/create").then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given().when().get("/items/count").then().statusCode(HttpStatus.SC_OK).body(is("1"));
    }

    @Test
    void testStatelessSessionGetMultiple() {
        // this also tests using JPA specific properties:
        // - jakarta.persistence.schema-generation.database.action
        // - jakarta.persistence.sql-load-script-source
        app.given()
                .queryParam("ids", List.of(1, 2, 3))
                .get("/test-data/get-multiple")
                .then().statusCode(HttpStatus.SC_OK)
                .body(Matchers.is("DataOne-DataThree-DataTwo"));
    }

    @Test
    void testHqlJsonAggFunction() {
        // this function is incubating, but JSON functions were mentioned in "What's new"
        // if it gets removed, we can safely remove it as well
        record JsonResult(String name) {
        }
        var results = app.given()
                .contentType(ContentType.JSON)
                .get("/test-data/json-agg-hql-function")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().as(JsonResult[].class);
        Assertions.assertEquals(3, results.length);
        Consumer<String> assertAnyHasName = name -> Assertions.assertTrue(
                Arrays.stream(results).anyMatch(r -> name.equals(r.name)),
                () -> "One of results should have property name set to " + name + ", but got: " + Arrays.toString(results));
        assertAnyHasName.accept("Jose");
        assertAnyHasName.accept("Jonathan");
        assertAnyHasName.accept("Julia");
    }

    @Test
    void testEnumConverter() {
        app.given()
                .queryParam("enums", List.of(1 + "-" + TestDataEntity.Character.NEW,
                        2 + "-" + TestDataEntity.Character.OLD,
                        3 + "-" + TestDataEntity.Character.UPDATED))
                .put("/test-data/upsert-enum-values")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given()
                .queryParam("character", TestDataEntity.Character.NEW.name())
                .get("/test-data/get-id-by-enum-in-where-clause")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("1"));
        app.given()
                .queryParam("character", TestDataEntity.Character.OLD.name())
                .get("/test-data/get-id-by-enum-in-where-clause")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("2"));
        app.given()
                .queryParam("character", TestDataEntity.Character.UPDATED.name())
                .get("/test-data/get-id-by-enum-in-where-clause")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("3"));
    }

    @Test
    void testFindReadOnlyOptionObject() {
        // assert initial state
        callFindWithOptionalReadOnly().body(is("DataOne"));
        // try to modify read-only object and expect that changes are not persisted
        callFindWithOptionalReadOnly(true, "DataWhatever");
        callFindWithOptionalReadOnly().body(is("DataOne"));
        // now try read and write mode and expect that changes are persisted
        callFindWithOptionalReadOnly(false, "DataWhatever");
        callFindWithOptionalReadOnly().body(is("DataWhatever"));
        // now clean up => put back original value
        callFindWithOptionalReadOnly(false, "DataOne");
        callFindWithOptionalReadOnly().body(is("DataOne"));
    }

    @Test
    void testSoftDeleteTimestampStrategy() {
        app.given()
                .pathParam("id", 1)
                .post("/soft-delete/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given()
                .pathParam("id", 1)
                .get("/soft-delete/{id}")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("1"));
        app.given()
                .pathParam("id", 1)
                .get("/soft-delete/{id}/get-timestamp")
                .then().statusCode(HttpStatus.SC_OK)
                .body(Matchers.emptyString());
        app.given()
                .pathParam("id", 1)
                .delete("/soft-delete/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given()
                .pathParam("id", 1)
                .get("/soft-delete/{id}/get-timestamp")
                .then().statusCode(HttpStatus.SC_OK)
                .body(Matchers.startsWith(Integer.toString(LocalDateTime.now().getYear())));
        app.given()
                .pathParam("id", 1)
                .get("/soft-delete/{id}")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void testEmbeddedColumnNaming() {
        // create entity
        app.given()
                .pathParam("id", 2)
                .post("/soft-delete/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // set work address (it is embedded field)
        app.given()
                .pathParam("id", 2)
                .queryParam("street", "Main Street")
                .queryParam("city", "New York City")
                .queryParam("state", "New York State")
                .queryParam("zip", "12345")
                .put("/soft-delete/{id}/work-address")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // set home address (it is embedded field)
        app.given()
                .pathParam("id", 2)
                .queryParam("street", "Second Avenue")
                .queryParam("city", "New York City")
                .queryParam("state", "New York State")
                .queryParam("zip", "12345")
                .put("/soft-delete/{id}/home-address")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // test work address embedde field name
        app.given()
                .pathParam("id", 2)
                .get("/soft-delete/{id}/get-work-address-street")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Main Street"));
        app.given()
                .pathParam("id", 2)
                .get("/soft-delete/{id}/get-home-address-street")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Second Avenue"));
    }

    @Test
    void testNamedEntityGraph() {
        // custom is lazy loaded, but because we use named graph, it works without fetching
        app.given()
                .pathParam("id", 1)
                .get("/items/{id}")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("1"));
    }

    @Test
    void test2ndLevelCacheQueryCacheableOptionForStatelessSession() {
        // tests query "cacheable" option that if enabled, sets the cacheable hint as well as region, mode etc.
        int idAsInt = RandomGenerator.getDefault().nextInt();
        String id = Integer.toString(idAsInt);
        // create
        app.given()
                .pathParam("id", id)
                .body("Troubled Man")
                .post("/stateless-session/insert/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // get using query with cacheable set to "true"
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", true)
                .get("/stateless-session/get-content-using-query-with-cacheable-option")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Troubled Man"));
        // update the database table using JDBC connection directly, so that Hibernate is not aware of the change
        app.given()
                .put("/stateless-session/update-using-connection-directly")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // query has "cacheable" set to "true", so we won't see the database changes
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", true)
                .get("/stateless-session/get-content-using-query-with-cacheable-option")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Troubled Man"));
        // however if we set the "cacheable" hint to "false", we query database and see actual value
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", false)
                .get("/stateless-session/get-content-using-query-with-cacheable-option")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Troubled Man".repeat(2)));
        // clean up
        app.given()
                .pathParam("id", id)
                .delete("/stateless-session/delete/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", false)
                .get("/stateless-session/get-content-using-query-with-cacheable-option")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    void test2ndLevelCacheQueryHintsForStatelessSession() {
        int idAsInt = RandomGenerator.getDefault().nextInt();
        String id = Integer.toString(idAsInt);
        // create
        app.given()
                .pathParam("id", id)
                .body("Troubled Man")
                .post("/stateless-session/insert/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // get using query with cacheable set to "true"
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", true)
                .get("/stateless-session/get-content-using-query-with-cacheable-hint")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Troubled Man"));
        // update the database table using JDBC connection directly, so that Hibernate is not aware of the change
        app.given()
                .put("/stateless-session/update-using-connection-directly")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        // query has "cacheable" set to "true", so we won't see the database changes
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", true)
                .get("/stateless-session/get-content-using-query-with-cacheable-hint")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Troubled Man"));
        // however if we set the "cacheable" hint to "false", we query database and see actual value
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", false)
                .get("/stateless-session/get-content-using-query-with-cacheable-hint")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("Troubled Man".repeat(2)));
        // clean up
        app.given()
                .pathParam("id", id)
                .delete("/stateless-session/delete/{id}")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        app.given()
                .queryParam("id", id)
                .queryParam("cacheable", false)
                .get("/stateless-session/get-content-using-query-with-cacheable-hint")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Tag("QUARKUS-6545")
    @Test
    void testSessionMergeEvents() {
        // create instance
        app.given()
                .body("Some data")
                .post("/session/persist/6006")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // assert its there
        app.given()
                .get("/session/find/6006")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Some data"));
        // merge instance
        app.given()
                .body("Merger data")
                .post("/session/merge/6006")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // assert its there and delete it
        app.given()
                .get("/session/find/6006")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Merger data"));
        app.given()
                .delete("/session/remove/6006")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("true"));
        app.given()
                .get("/session/get-reference/6006")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
        // check that Hibernate fired respective events
        var mergeData = app.given()
                .get("/session/merge/intercepted-data")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.notNullValue())
                .extract().as(SessionEventInterceptor.MergeData.class);
        assertEquals("data", mergeData.changedPropertyName());
        assertEquals("Some data", mergeData.originalState());
        assertEquals("Merger data", mergeData.targetState());
        assertEquals(6006, mergeData.entity().getAnId().getVal());
        assertEquals("Merger data", mergeData.entity().getData());
    }

    private void givenPostConstructAndPreDestroyAreNotInvoked() {
        assertEquals(FALSE, getPostConstructInvokeResult(), "PostConstruct method has been invoked already");
        assertEquals(FALSE, getPreDestroyInvokeResult(), "PreDestroy method has been invoked already");
    }

    private static void whenInvokeBean() {
        app.given().when().post(transactionScopePath("/invoke-bean")).then().body(is(EXPECTED_RESPONSE_FROM_INVOKE_BEAN));
    }

    private static void thenIsPostConstructInvoked() {
        assertEquals(TRUE, getPostConstructInvokeResult(), "PostConstruct method is not invoked");
    }

    private static void thenIsPreDestroyInvoked() {
        assertEquals(TRUE, getPreDestroyInvokeResult(), "PreDestroy method is not invoked");
    }

    private static String getPostConstructInvokeResult() {
        return app.given().when().get(transactionScopePath("/is-post-construct-invoked")).asString();
    }

    private static String getPreDestroyInvokeResult() {
        return app.given().when().get(transactionScopePath("/is-pre-destroy-invoked")).asString();
    }

    private static String transactionScopePath(String path) {
        return TRANSACTION_SCOPE_BASE_PATH + path;
    }

    private static ValidatableResponse callFindWithOptionalReadOnly() {
        return callFindWithOptionalReadOnly(true, null);
    }

    private static ValidatableResponse callFindWithOptionalReadOnly(boolean readOnly, String newContent) {
        var request = app.given()
                .queryParam("id", 1)
                .queryParam("read-only", readOnly);
        if (newContent != null && !newContent.isEmpty()) {
            request.queryParam("new-content", newContent);
        }
        return request
                .get("/test-data/find-with-optional-read-only")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testFlushListenerFiresOnNoOpFlush() {

        app.given()
                .post("/flush-events/reset")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        app.given()
                .get("/flush-events/trigger-noop-flush")
                .then()
                .statusCode(HttpStatus.SC_OK);

        FlushEventListener.FlushEventData data = getFlushEventData();

        assertTrue(data.flushCount() >= 1,
                "Hibernate 7.2 must trigger flush events even for read-only (no-op) flushes. Got: " + data.flushCount());
    }

    private FlushEventListener.FlushEventData getFlushEventData() {
        return app.given()
                .get("/flush-events/data")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(FlushEventListener.FlushEventData.class);
    }

    @Test
    public void testInTransactionHelper() {
        int testId = 7001;
        app.given()
                .body("Test data for inTransaction")
                .pathParam("id", testId)
                .post("/session/in-transaction/{id}")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        app.given()
                .pathParam("id", testId)
                .get("/session/find/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Test data for inTransaction"));
        removeEntity(testId);
    }

    @Test
    public void testFromTransactionFind() {
        int testId = 7002;
        app.given()
                .body("Test data for fromTransaction find")
                .pathParam("id", testId)
                .post("/session/persist/{id}")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        app.given()
                .pathParam("id", testId)
                .get("/session/from-transaction/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Test data for fromTransaction find"));
        removeEntity(testId);
    }

    private static void removeEntity(int id) {
        app.given()
                .pathParam("id", id)
                .delete("/session/remove/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void testHqlRegexpOperators() {
        int id1 = 7007;
        int id2 = 7008;
        app.given()
                .contentType(ContentType.TEXT)
                .body("Test123")
                .pathParam("id", id1)
                .post("/session/persist/{id}").then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        app.given()
                .contentType(ContentType.TEXT)
                .body("test456")
                .pathParam("id", id2)
                .post("/session/persist/{id}")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        String likeResult = app.given()
                .contentType(ContentType.TEXT)
                .body("Test[0-9]+")
                .post("/session/like-regexp")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();
        Assertions.assertTrue(likeResult.contains("Test123"), "like regexp should match Test123 (case-sensitive)");
        Assertions.assertFalse(likeResult.contains("test456"), "like regexp should not match test456 (case-sensitive)");

        String ilikeResult = app.given()
                .contentType(ContentType.TEXT)
                .body("test[0-9]+")
                .post("/session/ilike-regexp")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();
        Assertions.assertTrue(ilikeResult.contains("Test123"), "ilike regexp should match Test123 (case-insensitive)");
        Assertions.assertTrue(ilikeResult.contains("test456"), "ilike regexp should match test456 (case-insensitive)");

        app.given().pathParam("id", id1).delete("/session/remove/{id}").then().statusCode(HttpStatus.SC_OK);
        app.given().pathParam("id", id2).delete("/session/remove/{id}").then().statusCode(HttpStatus.SC_OK);
    }
}
