package io.quarkus.ts.jakarta.data.security;

import static org.hamcrest.Matchers.is;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DatabaseService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.jakarta.data.db.MyBook;
import io.restassured.http.ContentType;

public abstract class AbstractJakartaDataSecurityIT {

    public static final String BOOK_TITLE = "secure-book";

    protected static <T extends RestService> T createApp(Supplier<T> serviceCreator,
            DatabaseService<?> database, String dataSourceName) {
        var svc = serviceCreator.get();
        svc
                .withProperty("db-url", () -> database.getJdbcUrl())
                .withProperty("db-password", database::getPassword)
                .withProperty("db-username", database::getUser)
                .withProperty("quarkus.profile", dataSourceName);
        return svc;
    }

    protected static RestService createApp(DatabaseService<?> database, String dataSourceName) {
        return createApp(RestService::new, database, dataSourceName);
    }

    @LookupService
    static RestService app;

    @BeforeAll
    public static void setupBook() {
        createBook(BOOK_TITLE);
    }

    @Test
    public void testAllowAccessToRepositoryWithRolesAllowed() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/type-role")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryWithRolesAllowed() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/type-role")
                .then().statusCode(401);
    }

    @Test
    public void testRejectWrongRoleAccessToRepositoryWithRolesAllowed() {
        app.given()
                .auth().preemptive().basic("bob", "bob")
                .get("/book/" + BOOK_TITLE + "/type-role")
                .then().statusCode(403);
    }

    @Test
    public void testAllowAccessToRepositoryWithAuthenticated() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/type-authenticated")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryWithAuthenticated() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/type-authenticated")
                .then()
                .statusCode(401);
    }

    @Test
    public void testRejectAccessToRepositoryWithDenyAll() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/type-denyAll")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAllowAccessToRepositoryWithPermissionsAllowed() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/type-permission")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testRejectAccessToRepositoryWithPermissionsAllowedForUserWithoutPermission() {
        app.given()
                .auth().preemptive().basic("bob", "bob")
                .get("/book/" + BOOK_TITLE + "/type-permission")
                .then()
                .statusCode(403);
    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryWithPermissionsAllowed() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/type-permission")
                .then()
                .statusCode(401);
    }

    @Test
    public void testAllowAccessToRepositoryMethodWithPermissionsAllowed() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/method-permission")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));

    }

    @Test
    public void testRejectAccessToRepositoryMethodWithPermissionsAllowedForUserWithoutPermission() {
        app.given()
                .auth().preemptive().basic("bob", "bob")
                .get("/book/" + BOOK_TITLE + "/method-permission")
                .then()
                .statusCode(403);
    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryMethodWithPermissionsAllowed() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/method-permission")
                .then()
                .statusCode(401);
    }

    @Test
    public void testAllowAccessToRepositoryMethodWithAuthenticated() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/method-authenticated")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testRejectUnauthenticatedAccessMethodWithAuthenticated() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/method-authenticated")
                .then()
                .statusCode(401);
    }

    @Test
    public void testRejectAccessToRepositoryMethodWithDenyAll() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/method-denyAll")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAllowInsertWithWriterRole() {
        app.given()
                .auth().preemptive().basic("bob", "bob")
                .body(new MyBook("insert-book1"))
                .contentType(ContentType.JSON)
                .post("/book/add/role")
                .then()
                .statusCode(204);

        app.given()
                .get("/book/insert-book1/")
                .then()
                .statusCode(200)
                .body("title", is("insert-book1"));
    }

    @Test
    public void testRejectInsertWithoutWriterRole() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .body(new MyBook("insert-book2"))
                .contentType(ContentType.JSON)
                .post("/book/add/role")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAllowInsertWithPermission() {
        app.given()
                .auth().preemptive().basic("andy", "andy")
                .body(new MyBook("insert-book3"))
                .contentType(ContentType.JSON)
                .post("/book/add/permission")
                .then()
                .statusCode(204);

        app.given()
                .get("/book/insert-book3/")
                .then()
                .statusCode(200)
                .body("title", is("insert-book3"));
    }

    @Test
    public void testRejectInsertWithoutPermission() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .body(new MyBook("insert-book4"))
                .contentType(ContentType.JSON)
                .post("/book/add/permission")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAllowAccessToRepositoryWithInheritedTypeSecurity() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/inherited-type")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));

    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryWithInheritedTypeSecurity() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/inherited-type")
                .then()
                .statusCode(401);
    }

    @Test
    public void testAllowAccessToRepositoryWithInheritedMethodSecurity() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/inherited-method")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryWithInheritedMethodSecurity() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/inherited-method")
                .then()
                .statusCode(401);
    }

    @Test
    public void testAllowAccessToRepositoryMethodWithMultiplePermissionsAllowed() {
        app.given()
                .auth().preemptive().basic("andy", "andy")
                .get("/book/" + BOOK_TITLE + "/method-multiple-permissions")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testRejectAccessToRepositoryMethodWithMultiplePermissionsAllowed() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/method-multiple-permissions")
                .then()
                .statusCode(403);
    }

    @Test
    public void testRejectUnauthenticatedAccessToRepositoryMethodWithMultiplePermissionsAllowed() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/method-multiple-permissions")
                .then()
                .statusCode(401);
    }

    @Test
    public void testSecurityPrecedenceAllowed() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/precedence")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testSecurityPrecedenceWrongRole() {
        app.given()
                .auth().preemptive().basic("bob", "bob")
                .get("/book/" + BOOK_TITLE + "/precedence")
                .then()
                .statusCode(403);
    }

    @Test
    public void testSecurityPrecedenceUnauthenticated() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/precedence")
                .then()
                .statusCode(401);
    }

    @Test
    public void testOverriddenMethodAllowed() {
        app.given()
                .auth().preemptive().basic("bob", "bob")
                .get("/book/" + BOOK_TITLE + "/override")
                .then()
                .statusCode(200)
                .body("title", is(BOOK_TITLE));
    }

    @Test
    public void testOverriddenMethodWrongRole() {
        app.given()
                .auth().preemptive().basic("alice", "alice")
                .get("/book/" + BOOK_TITLE + "/override")
                .then()
                .statusCode(403);
    }

    @Test
    public void testOverriddenMethodUnauthenticated() {
        app.given()
                .get("/book/" + BOOK_TITLE + "/override")
                .then()
                .statusCode(401);
    }

    @Test
    public void testAllowUpdateWithWriterRole() {
        createBook("update-book1");

        app.given()
                .auth().preemptive().basic("bob", "bob")
                .body(new MyBook("updated-book1"))
                .contentType(ContentType.JSON)
                .put("/book/update-book1/update/writer")
                .then()
                .statusCode(200)
                .body("title", is("updated-book1"));
    }

    @Test
    public void testRejectUpdateWithoutWriterRole() {
        createBook("update-book2");

        app.given()
                .auth().preemptive().basic("alice", "alice")
                .body(new MyBook("updated-book2"))
                .contentType(ContentType.JSON)
                .put("/book/update-book2/update/writer")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAllowDeleteWithPermission() {
        createBook("delete-book1");

        app.given()
                .auth().preemptive().basic("andy", "andy")
                .delete("/book/delete-book1/delete/permission")
                .then()
                .statusCode(204);
    }

    @Test
    public void testRejectDeleteWithoutPermission() {
        createBook("delete-book2");

        app.given()
                .auth().preemptive().basic("alice", "alice")
                .delete("/book/delete-book2/delete/permission")
                .then()
                .statusCode(403);
    }

    private static void createBook(String title) {
        app.given()
                .body(new MyBook(title))
                .contentType(ContentType.JSON)
                .post("/book/add")
                .then()
                .statusCode(204);
    }
}
