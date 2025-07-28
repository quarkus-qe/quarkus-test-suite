package io.quarkus.ts.security.jpa.reactive;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.jpa.reactive.db.MyEntity;
import io.quarkus.ts.security.jpa.reactive.rest.CreateEntityRequestDto;
import io.quarkus.ts.security.jpa.reactive.rest.UpdateEntityRequestDto;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

public abstract class AbstractSecurityJpaReactiveIT {

    @LookupService
    static PostgresqlService database;

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.reactive.url", () -> database.getReactiveUrl())
            .withProperty("quarkus.datasource.jdbc.url", () -> database.getJdbcUrl())
            .withProperty("quarkus.datasource.password", () -> database.getPassword())
            .withProperty("quarkus.datasource.username", () -> database.getUser());

    @Test
    void testRolesAllowedAnnotation() {
        Integer entityId = null;
        try {
            var requestDto = new CreateEntityRequestDto("Entity name 1", "entity-name-1@quarkus.io");
            // this endpoint is annotated with @RolesAllowed("admin")
            // anonymous user is unauthorized
            create(requestDto).statusCode(401);
            // user don't have 'admin' role
            create("user", requestDto).statusCode(403);
            // admin have required 'admin' role
            entityId = create("admin", requestDto).statusCode(200)
                    .body("id", notNullValue())
                    .body("name", is(requestDto.name()))
                    .body("email", is(requestDto.email()))
                    .extract().response().path("id");
        } finally {
            if (entityId != null) {
                // clean-up
                deleteEntity(entityId);
            }
        }
    }

    @Test
    void testPermissionsAllowedAnnotation() {
        Integer entityId = null;
        try {
            // this endpoint is annotated with @PermissionsAllowed("can-update") and permission check that
            // all update requests are authenticated and if someone wants to update the email to 'security@quarkus.io'
            // they must have 'admin' role; basic authentication mechanism is selected with @BasicAuthentication
            var createEntityDto = new CreateEntityRequestDto("Entity name 2", "entity-name-2@quarkus.io");
            entityId = create("admin", createEntityDto).statusCode(200).extract().response().path("id");

            var updateEntityDto = new UpdateEntityRequestDto("security@quarkus.io");
            // anonymous request is unauthorized
            update(updateEntityDto, entityId).statusCode(401);
            // user cannot set the email to 'security@quarkus.io', only admin can do that
            update("user", updateEntityDto, entityId).statusCode(403);
            // however user can update the email to something else
            var otherEmailUpdateEntityDto = new UpdateEntityRequestDto("qe@quarkus.io");
            update("user", otherEmailUpdateEntityDto, entityId).statusCode(200)
                    .body("email", is(otherEmailUpdateEntityDto.email()));
            // admin can update the email to any value
            update("admin", updateEntityDto, entityId).statusCode(200)
                    .body("email", is(updateEntityDto.email()));
        } finally {
            if (entityId != null) {
                // clean-up
                deleteEntity(entityId);
            }
        }
    }

    @Test
    void testHttpPermissionCustomPolicy() {
        Integer entityId1 = null;
        Integer entityId2 = null;
        Integer entityId3 = null;

        try {
            // this route (or more specifically this path & HTTP method combination) are secured with a custom policy
            // the policy requires that the identity has either 'user' role or request has header 'trust-me-please'
            entityId1 = create("admin", new CreateEntityRequestDto("Name 1", "name-1@quarkus.io")).statusCode(200).extract()
                    .path("id");
            entityId2 = create("admin", new CreateEntityRequestDto("Name 2", "name-2@quarkus.io")).statusCode(200).extract()
                    .path("id");
            entityId3 = create("admin", new CreateEntityRequestDto("Name 3", "name-3@quarkus.io")).statusCode(200).extract()
                    .path("id");

            app.given().get("/crud").then().statusCode(401);
            app.given().auth().preemptive().basic("admin", "admin").get("/crud").then().statusCode(403);
            app.given().auth().preemptive().basic("admin", "admin").header("trust-me-please", "ignored").get("/crud").then()
                    .statusCode(200).body("size()", is(3));
            app.given().auth().preemptive().basic("user", "user").get("/crud").then().statusCode(200).body("size()", is(3));
        } finally {
            // clean-up
            if (entityId1 != null) {
                deleteEntity(entityId1);
            }
            if (entityId2 != null) {
                deleteEntity(entityId2);
            }
            if (entityId3 != null) {
                deleteEntity(entityId3);
            }
        }
    }

    @Test
    void testHttpPermissionRolesAllowed() {
        Integer entityId1 = null;
        try {
            // this route is secured with HTTP permission that only permits access to SecurityIdentity with role 'admin'
            var requestDto = new CreateEntityRequestDto("Name 1", "name-1@quarkus.io");
            entityId1 = create("admin", requestDto).statusCode(200).extract().path("id");

            app.given().get("/crud/detail/" + entityId1).then().statusCode(401);
            app.given().auth().preemptive().basic("user", "user").get("/crud/detail/" + entityId1).then().statusCode(403);
            app.given().auth().preemptive().basic("admin", "admin").get("/crud/detail/" + entityId1).then().statusCode(200)
                    .body("id", is(entityId1))
                    .body("name", is(requestDto.name()))
                    .body("email", is(requestDto.email()));
        } finally {
            // clean-up
            if (entityId1 != null) {
                deleteEntity(entityId1);
            }
        }
    }

    @Test
    void testFormBasedAuthentication() {
        Integer entityId = null;
        try {
            // form-based authentication is required because the endpoint is annotated with the @FormAuthentication annotation
            var requestDto = new CreateEntityRequestDto("Name 1", "name-1@quarkus.io");
            var entity = create("admin", requestDto).statusCode(200)
                    .body("id", notNullValue())
                    .body("name", is(requestDto.name()))
                    .body("email", is(requestDto.email()))
                    .extract().as(MyEntity.class);
            entityId = entity.id.intValue();

            // change entity because we need to test the patch method
            entity.email = "dev@quarkus.io";
            entity.name = "dev";

            // anonymous request is not allowed
            app.given().body(entity).redirects().follow(false).patch("/crud").then().statusCode(302);
            // basic authentication is not supported here
            app.given().body(entity).redirects().follow(false).auth().preemptive().basic("user", "user").patch("/crud").then()
                    .statusCode(302);

            // now use form-based authentication
            CookieFilter cookies = new CookieFilter();
            app.given().filter(cookies).redirects().follow(false).when()
                    .formParam("j_username", "user")
                    .formParam("j_password", "user")
                    .post("/j_security_check")
                    .then()
                    .assertThat()
                    .statusCode(302);
            app.given().body(entity).filter(cookies).contentType(ContentType.JSON).patch("/crud").then().statusCode(200)
                    .body("id", is(entity.id.intValue()))
                    .body("name", is(entity.name))
                    .body("email", is(entity.email));
        } finally {
            if (entityId != null) {
                deleteEntity(entityId);
            }
        }
    }

    private static ValidatableResponse update(UpdateEntityRequestDto updateEntityDto, int entityId) {
        return update(null, updateEntityDto, entityId);
    }

    private static ValidatableResponse update(String user, UpdateEntityRequestDto updateEntityDto, int entityId) {
        var request = app.given().body(updateEntityDto).contentType(ContentType.JSON);
        if (user != null) {
            request.auth().preemptive().basic(user, user);
        }
        return request.put("/crud/" + entityId).then();
    }

    private static ValidatableResponse create(CreateEntityRequestDto createEntityDto) {
        return create(null, createEntityDto);
    }

    private static ValidatableResponse create(String user, CreateEntityRequestDto createEntityDto) {
        var request = app.given()
                .body(createEntityDto)
                .contentType(ContentType.JSON);
        if (user != null) {
            request.auth().preemptive().basic(user, user);
        }
        return request.post("/crud").then();
    }

    private static void deleteEntity(long id) {
        app.given()
                .delete("/crud/" + id)
                .then().statusCode(200)
                .body(is("true"));
    }
}
