package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public abstract class AbstractPagingAndSortingRepositoryRestResourceIT extends AbstractRepositoryRestResourceIT {

    protected static final List<String> ORIGINAL_ITEMS = Arrays.asList("Aeneid", "Beach House", "Cadillac Desert",
            "Dagon and Other Macabre Tales");
    protected static final String CREATED_ITEM = "Early Asimov";
    protected static final String UPDATED_ITEM = "Early Asimov 2nd Edition";

    protected abstract List<String> getItemsAfterUpdates();

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
        return 5;
    }

    @Override
    protected String getUpdatedItem() {
        return UPDATED_ITEM;
    }

    @Order(8)
    @Test
    void pagination() {
        //Test repository pagination
        testGetPage(2, 0, getItemsAfterUpdates());
        testGetPage(2, 1, getItemsAfterUpdates());
        testGetPage(2, 2, getItemsAfterUpdates());
        testGetPage(5, 2, getItemsAfterUpdates());
    }

    @Order(9)
    @Test
    void sorting() {
        //Test repository sorting
        List<String> articleNamesSortedDesc = new ArrayList<>(getItemsAfterUpdates());
        articleNamesSortedDesc.sort(Comparator.reverseOrder());
        Response response = app.given()
                .accept("application/json")
                .queryParam("sort", "-name")
                .when().get(getUrl())
                .then()
                .statusCode(HttpStatus.SC_OK).extract().response();
        List<String> articleNamesRepositorySortedDesc = response.jsonPath().getList("name");
        assertEquals(articleNamesSortedDesc, articleNamesRepositorySortedDesc);
    }

    @Test
    void createWithEntityConstraintViolation() {
        //Try to add a article with invalid constraints
        app.given()
                .contentType("application/json")
                .body("{\"name\": \"Q\", \"author\": \"Li\"}")
                .when().post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(containsString("length must be between 2 and 50"));
    }

    private void testGetPage(long pageSize, long pageNumber, List<String> allItems) {
        ValidatableResponse response = app.given()
                .accept("application/json")
                .queryParam("size", pageSize)
                .queryParam("page", pageNumber)
                .when().get(getUrl())
                .then()
                .statusCode(HttpStatus.SC_OK);
        // items from pages before pageNumber must not be present
        allItems.stream()
                .limit(pageSize * pageNumber)
                .map(CoreMatchers::containsString)
                .map(CoreMatchers::not)
                .forEach(response::body);
        // items from page pageNumber must be present
        allItems.stream()
                .skip(pageSize * pageNumber)
                .limit(pageSize)
                .map(CoreMatchers::containsString)
                .forEach(response::body);
        // items from pages after pageNumber must be present
        allItems.stream()
                .skip(pageSize * (pageNumber + 1))
                .map(CoreMatchers::containsString)
                .map(CoreMatchers::not)
                .forEach(response::body);
    }

}
