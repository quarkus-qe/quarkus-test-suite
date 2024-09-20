package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

@QuarkusScenario
public class CrudRepositoryRestResourceIT extends AbstractRepositoryRestResourceIT {

    protected static final List<String> ORIGINAL_ITEMS = List.of("Library1");
    protected static final String CREATED_ITEM = "Library2";
    protected static final String UPDATED_ITEM = "Library Two";

    @Override
    protected String getUrl() {
        return "/library";
    }

    @Override
    protected List<String> getOriginalItems() {
        return ORIGINAL_ITEMS;
    }

    @Override
    protected String getCreatedItem() {
        return CREATED_ITEM;
    }

    @Override
    protected long getCreatedItemId() {
        return 2;
    }

    @Override
    protected String getUpdatedItem() {
        return UPDATED_ITEM;
    }

    @Override
    protected String getItemNameHalJsonPath() {
        return "_embedded.library.name";
    }

    @Override
    protected String getItemIdUrl(long id) {
        return getUrl() + "/id/" + id;
    }

    @Test
    void createWithEntityConstraintViolation() {
        //Try to add a article with invalid constraints
        app.given()
                .contentType("application/json")
                .body("{\"name\": \"\"}")
                .when().post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("Name may not be blank"));
    }

    @Tag("QUARKUS-4958")
    @Test
    public void testListCrudRepository_findAllMethod() {
        var magazines = app.given()
                .accept(ContentType.JSON)
                .get("magazine-list-crud-rest-repository")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(new TypeRef<List<Magazine>>() {
                });
        assertEquals(6, magazines.size());
        assertTrue(hasMagazineWithTitle(magazines, "Vanity Fair"));
        assertTrue(hasMagazineWithTitle(magazines, "The Economist"));
    }

    @Tag("QUARKUS-4958")
    @Test
    public void testListCrudRepository_Transactional() {
        // tests @Transactional for multiple operations:
        // custom method findByName, findById, save
        // because Quarkus automatically activates transaction
        // for individual methods if we don't declare our annotation,
        // but we want to try them all in one transaction

        // anonymous user
        app.given()
                .contentType(ContentType.JSON)
                .put("/magazine-resource")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
        // authentication user with insufficient rights
        app.given()
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("user", "user")
                .put("/magazine-resource")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        // test authorized user
        app.given()
                .contentType(ContentType.JSON)
                .auth().preemptive().basic("admin", "admin")
                .body(new MagazineResource.MagazineDto("Witch Weekly", "Harper's Monthly"))
                .put("/magazine-resource")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Harper's Monthly"));
    }

    private static boolean hasMagazineWithTitle(List<Magazine> magazines, String title) {
        return magazines.stream().anyMatch(m -> title.equals(m.getName()));
    }
}
