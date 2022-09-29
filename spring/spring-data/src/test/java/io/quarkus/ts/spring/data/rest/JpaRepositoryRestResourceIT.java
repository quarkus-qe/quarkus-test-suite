package io.quarkus.ts.spring.data.rest;

import java.util.List;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class JpaRepositoryRestResourceIT extends AbstractPagingAndSortingRepositoryRestResourceIT {

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
}
