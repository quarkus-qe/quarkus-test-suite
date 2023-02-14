package io.quarkus.ts.reactive.rest.data.panache;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractPanacheResourceIT {

    static final String APPLICATION_PATH = "/application";
    static final String EXPECTED_VERSION = "1.2.0";

    private final static String NEW_USER_ID = "3";

    private ApplicationEntity actualEntity;
    private ApplicationEntity[] actualList;

    @AfterEach
    public void tearDown() {
        deleteEntityIfExists();
    }

    @Test
    public void shouldCreateApplication() {
        whenCreateApplication("my-app-name");
        thenApplicationMatches("my-app-name");
    }

    @Test
    public void shouldUpdateApplication() {
        whenCreateApplication("my-app-name");
        whenUpdateApplication("another-app-name");
        thenApplicationMatches("another-app-name");
    }

    @Test
    public void shouldListApplications() {
        whenCreateApplication("my-app-name");
        whenGetApplications();
        thenApplicationsCountIs(1);
        thenApplicationsContainWithName("my-app-name");
    }

    @Test
    public void shouldDeleteApplication() {
        whenCreateApplication("my-app-name");
        whenDeleteApplication();
        whenGetApplications();
        thenApplicationsCountIs(0);
    }

    @Test
    public void shouldReturnBadRequestIfApplicationNameIsNull() {
        applicationPath().body(new ApplicationEntity()).post()
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("name can't be null"));
    }

    @Test
    public void shouldFindApplicationWhenFilteringByName() {
        whenCreateApplication("my-app-name");
        whenFilterApplicationByName("useLikeByName", "name", "%-app-%");
        thenApplicationsCountIs(1);
    }

    @Test
    public void shouldNotFindApplicationWhenFilteringByName() {
        whenCreateApplication("my-app-name");
        whenFilterApplicationByName("useLikeByName", "name", "%not-exist%");
        thenApplicationsCountIs(0);
    }

    @Test
    public void shouldFindApplicationWhenFilteringByServiceName() {
        givenApplicationWithServices("service-a", "service-b");
        whenFilterApplicationByName("useServiceByName", "name", "service-a");
        thenApplicationsContainService("service-a");
        thenApplicationsDoNotContainService("service-b");
    }

    private void givenApplicationWithServices(String... services) {
        whenCreateApplication("my-app-name");

        for (String service : services) {
            whenCreateService(service);
            thenServiceIsFound(service);
        }
    }

    private void whenCreateService(String serviceName) {
        ServiceEntity service = new ServiceEntity();
        service.name = serviceName;
        actualEntity.services.add(service);
        applicationPath().body(actualEntity).put("/" + actualEntity.id)
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
        whenGetApplication();
    }

    private void whenFilterApplicationByName(String filterName, String paramName, String paramValue) {
        String params = String.format("?%s=%s", paramName, paramValue);

        actualList = applicationPath().get("filterBy/" + filterName + params).then()
                .statusCode(HttpStatus.SC_OK).and().extract().as(ApplicationEntity[].class);
    }

    private void whenCreateApplication(String appName) {
        ApplicationEntity request = new ApplicationEntity();
        request.name = appName;
        request.version = new VersionEntity();
        request.version.id = EXPECTED_VERSION;

        actualEntity = applicationPath().body(request).post()
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .and().extract().as(ApplicationEntity.class);
    }

    private void whenUpdateApplication(String appName) {
        actualEntity.name = appName;
        applicationPath().body(actualEntity).put("/" + actualEntity.id)
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private void whenDeleteApplication() {
        applicationPath().delete("/" + actualEntity.id)
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        actualEntity = null;
    }

    private void whenGetApplication() {
        actualEntity = applicationPath().get("/" + actualEntity.id).then().statusCode(HttpStatus.SC_OK).and().extract()
                .as(ApplicationEntity.class);
    }

    private void whenGetApplications() {
        actualList = applicationPath().get().then().statusCode(HttpStatus.SC_OK).and().extract().as(ApplicationEntity[].class);
    }

    private void thenApplicationMatches(String expectedAppName) {
        assertNotNull(actualEntity.id);
        assertEquals(expectedAppName, actualEntity.name);
        assertEquals(EXPECTED_VERSION, actualEntity.version.id);
    }

    private void thenApplicationsCountIs(int expectedCount) {
        assertNotNull(actualList);
        assertEquals(expectedCount, actualList.length);
    }

    private void thenApplicationsContainWithName(String expectedAppName) {
        assertNotNull(actualList);
        assertTrue(Stream.of(actualList).allMatch(item -> expectedAppName.equals(item.name)));
    }

    private void thenApplicationsContainService(String expectedServiceName) {
        thenApplicationsMatchServicesCondition(actualServices -> actualServices.contains(expectedServiceName));
    }

    private void thenApplicationsDoNotContainService(String expectedServiceName) {
        thenApplicationsMatchServicesCondition(actualServices -> !actualServices.contains(expectedServiceName));
    }

    private void thenApplicationsMatchServicesCondition(Predicate<List<String>> servicePredicate) {
        for (ApplicationEntity actualApplication : actualList) {
            List<String> actualServices = actualApplication.services.stream().map(s -> s.name).collect(Collectors.toList());
            assertTrue(servicePredicate.test(actualServices), "Service not expected. Found: " + actualServices);
        }
    }

    private void thenServiceIsFound(String expectedServiceName) {
        assertNotNull(actualEntity.services);
        assertFalse(actualEntity.services.isEmpty(), "No services found");
        assertTrue(actualEntity.getServiceByName(expectedServiceName).isPresent());
    }

    private void deleteEntityIfExists() {
        if (actualEntity != null) {
            whenDeleteApplication();
        }
    }

    private static final RequestSpecification applicationPath() {
        return given().accept(MediaType.APPLICATION_JSON).contentType(ContentType.JSON).when().basePath(APPLICATION_PATH);
    }

    @Test
    void testAllRepositoryMethods() {

        //GET - List all users
        //Assert that order matches. UserRepository override it to be ascend. Initial order defined by import.sql
        String userList = given()
                .accept("application/hal+json")
                .when().get("/users/all")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().jsonPath().getString("_embedded.user_list.name");
        assertEquals("[Alaba, Balaba]", userList);

        //POST - Create a new User
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Culibaba\"}")
                .when().post("/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body(CoreMatchers.containsString("Culibaba"))
                .body("id", notNullValue());

        //PUT - Update a new User (method not allowed)
        given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\"name\": \"Donbaba\"}")
                .when().put("/users/" + NEW_USER_ID)
                .then()
                .statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);

        //GET{id} - Find new user by id
        given()
                .when().get("/users/" + NEW_USER_ID)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        CoreMatchers.containsString("Culibaba"));

        //DELETE - Delete new user via HTTP
        given()
                .when().delete("/users/" + NEW_USER_ID)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        //Test repository pagination
        given()
                .accept("application/json")
                .queryParam("size", "1")
                .queryParam("page", "1")
                .when().get("/users/all")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        not(CoreMatchers.containsString("Alaba")),
                        CoreMatchers.containsString("Balaba"));
    }

    @Test
    void testSortAscRepositoryQuery() {
        String userList = getUsersSorted("name");
        assertEquals("[Alaba, Balaba]", userList);
    }

    @Test
    void testSortDescRepositoryQuery() {
        String userList = getUsersSorted("-name");
        assertEquals("[Balaba, Alaba]", userList);
    }

    private String getUsersSorted(String sortField) {
        return given()
                .accept("application/hal+json")
                .when().get("/users/all?sort=" + sortField)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().jsonPath().getString("_embedded.user_list.name");
    }
}
