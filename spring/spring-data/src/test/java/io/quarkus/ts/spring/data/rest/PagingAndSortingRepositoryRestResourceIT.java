package io.quarkus.ts.spring.data.rest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;

import com.google.common.collect.Streams;

import io.quarkus.test.scenarios.QuarkusScenario;

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
}
