package io.quarkus.ts.spring.data.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Streams;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

@QuarkusScenario
class PagingAndSortingRepositoryRestResourceIT extends AbstractPagingAndSortingRepositoryRestResourceIT {

    private static final List<String> MODIFIED_ITEMS = Streams.concat(ORIGINAL_ITEMS.stream(), Stream.of(UPDATED_ITEM))
            .collect(Collectors.toList());

    @Override
    protected String getUrl() {
        return "/articles";
    }

    @Override
    protected List<String> getItemsAfterUpdates() {
        return MODIFIED_ITEMS;
    }

    @Override
    protected String getItemNameHalJsonPath() {
        return "_embedded.articles.name";
    }

    @Override
    protected String getItemIdUrl(long id) {
        return getUrl() + "/id/" + id;
    }

    @Override
    protected int getExpectedDeleteResponseStatus() {
        return HttpStatus.SC_METHOD_NOT_ALLOWED;
    }

    @Override
    protected int getExpectedDeleteInvalidResponseStatus() {
        return HttpStatus.SC_METHOD_NOT_ALLOWED;
    }

    @Tag("QUARKUS-4958")
    @Test
    public void testListPagingAndSortingRepository_onlyPagination() {
        // test size greater than actual number of items
        assertEquals(6, getListSize(0, 100));
        assertEquals(0, getListSize(1, 100));
        assertEquals(5, getListSize(0, 5));
        assertEquals(1, getListSize(1, 5));
        assertEquals(1, getListSize(0, 1));
        assertEquals(1, getListSize(1, 1));
        assertEquals(1, getListSize(2, 1));
        assertEquals(1, getListSize(3, 1));
        assertEquals(1, getListSize(4, 1));
        assertEquals(1, getListSize(5, 1));
        assertEquals(0, getListSize(6, 1));
    }

    @Tag("QUARKUS-4958")
    @Test
    public void testListPagingAndSortingRepository_onlySorting_sortByOneProperty() {
        assertAscending(getListSortedBy("name"));
        assertDescending(getListSortedBy("-name"));
    }

    @Tag("QUARKUS-4958")
    @Test
    public void testListPagingAndSortingRepository_onlySorting_sortByTwoProperties() {
        var ascendingIssuedIn = getMagazines(0, 6, "name,issuedIn");
        var descendingIssuedIn = getMagazines(0, 6, "name,-issuedIn");

        // both should be in ascending mode because primarily we sort by 'name'
        // and where 'issuedIn' differs is where names are same
        assertAscending(getMagazineNames(ascendingIssuedIn));
        assertAscending(getMagazineNames(descendingIssuedIn));

        // now make sure that secondary sorting by 'issuedIn' works
        var ascendingIssuedInTimeMagazines = keepTimeMagazinesOnly(ascendingIssuedIn);
        var descendingIssuedInTimeMagazines = keepTimeMagazinesOnly(descendingIssuedIn);
        assertEquals(2, ascendingIssuedInTimeMagazines.size());
        assertEquals(2, descendingIssuedInTimeMagazines.size());
        assertEquals(ascendingIssuedInTimeMagazines.get(0).getIssuedIn(), descendingIssuedInTimeMagazines.get(1).getIssuedIn());
        assertEquals(ascendingIssuedInTimeMagazines.get(1).getIssuedIn(), descendingIssuedInTimeMagazines.get(0).getIssuedIn());
        assertNotEquals(ascendingIssuedInTimeMagazines.get(0), ascendingIssuedInTimeMagazines.get(1));
    }

    @Tag("QUARKUS-4958")
    @Test
    public void testListPagingAndSortingRepository_sortingAndPagination() {
        var magazinesDesc = getMagazineNames(0, 3, "-name");
        assertDescending(magazinesDesc);
        var magazinesAsc = getMagazineNames(1, 3, "name");
        assertAscending(magazinesAsc);
        assertEquals(new HashSet<>(magazinesAsc), new HashSet<>(magazinesDesc));
    }

    private static void assertDescending(List<String> magazineNames) {
        var iterator = magazineNames.iterator();
        var previousItem = iterator.next();
        while (iterator.hasNext()) {
            var currentItem = iterator.next();
            assertTrue(currentItem.compareTo(previousItem) < 0);
        }
    }

    private static void assertAscending(List<String> magazineNames) {
        var iterator = magazineNames.iterator();
        var previousItem = iterator.next();
        while (iterator.hasNext()) {
            var currentItem = iterator.next();
            assertTrue(currentItem.compareTo(previousItem) > 0);
        }
    }

    private static List<String> getListSortedBy(String sortBy) {
        return getMagazineNames(null, null, sortBy);
    }

    private static int getListSize(int page, int size) {
        return getMagazines(page, size, null).size();
    }

    private static List<String> getMagazineNames(Integer page, Integer size, String sortBy) {
        return getMagazineNames(getMagazines(page, size, sortBy));
    }

    private static List<String> getMagazineNames(List<Magazine> magazines) {
        return magazines.stream().map(Magazine::getName).toList();
    }

    private static List<Magazine> getMagazines(Integer page, Integer size, String sortBy) {
        var req = app.given()
                .accept(ContentType.JSON);
        if (page != null) {
            req = req.queryParam("page", page);
        }
        if (size != null) {
            req = req.queryParam("size", size);
        }
        if (sortBy != null) {
            req = req.queryParam("sort", sortBy);
        }
        return req.get("magazine-list-paging-sorting-rest-repository")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    private static List<Magazine> keepTimeMagazinesOnly(List<Magazine> magazines) {
        return magazines.stream().filter(m -> "Time".equals(m.getName())).toList();
    }
}
