package io.quarkus.ts.spring.data.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class JpaRepositoryRestResourceIT extends AbstractPagingAndSortingRepositoryRestResourceIT {

    private static final String ALTERNATIVE_PREFIX = "Alternative ";

    @Override
    protected String getUrl() {
        return "/article-jpa";
    }

    @Override
    protected List<String> getItemsAfterUpdates() {
        return ORIGINAL_ITEMS;
    }

    @Override
    protected String getItemNameHalJsonPath() {
        return "_embedded.article-jpa.name";
    }

    @Override
    protected String getItemIdUrl(long id) {
        return getUrl() + "/" + id;
    }

    @Test
    public void testNamedDataSourceJpaRepository() {
        List<String> actualItems = app.given()
                .accept("application/json")
                .when().get("/alternative-article-jpa")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("collect{it.name}");
        assertEquals(ORIGINAL_ITEMS.size(), actualItems.size());
        Set<String> expectedItems = ORIGINAL_ITEMS
                .stream()
                .map(ALTERNATIVE_PREFIX::concat)
                .collect(Collectors.toSet());
        assertTrue(expectedItems.containsAll(actualItems));
    }

}
