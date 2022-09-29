package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

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
}
