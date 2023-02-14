package io.quarkus.ts.hibernate.search;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

public abstract class AbstractMultitenantHibernateSearchIT {
    public static final TypeRef<List<Fruit>> FRUIT_LIST_TYPE_REF = new TypeRef<>() {
    };

    private static final List EMPTY_LIST = List.of();

    @Test
    public void fullTextSearch() {
        String tenant1Id = "company1";
        String tenant2Id = "company2";
        String fruitName = "myFruit";

        // Check the indexes are empty
        assertThat(search(tenant1Id, fruitName), is(EMPTY_LIST));
        assertThat(search(tenant2Id, fruitName), is(EMPTY_LIST));

        // Create fruit for tenant 1
        Fruit fruit1 = new Fruit(fruitName);
        create(tenant1Id, fruit1);
        assertThat(search(tenant1Id, fruitName), hasSize(equalTo(1)));
        assertThat(search(tenant2Id, fruitName), is(EMPTY_LIST));

        // Create fruit for tenant 2
        Fruit fruit2 = new Fruit(fruitName);
        create(tenant2Id, fruit2);
        assertThat(search(tenant1Id, fruitName), hasSize(equalTo(1)));
        assertThat(search(tenant2Id, fruitName), hasSize(equalTo(1)));

        // Update fruit for tenant 1
        fruit1 = search(tenant1Id, fruitName).get(0);
        fruit1.setName("newName");
        update(tenant1Id, fruit1);
        assertThat(search(tenant1Id, fruitName), is(EMPTY_LIST));
        assertThat(search(tenant1Id, "newName"), hasSize(equalTo(1)));
        assertThat(search(tenant2Id, fruitName), hasSize(equalTo(1)));
        assertThat(search(tenant2Id, "newName"), is(EMPTY_LIST));

        // Delete fruit for tenant 2
        fruit2 = search(tenant2Id, fruitName).get(0);
        delete(tenant2Id, fruit2);
        assertThat(search(tenant1Id, fruitName), is(EMPTY_LIST));
        assertThat(search(tenant2Id, fruitName), is(EMPTY_LIST));
    }

    @Test
    public void fullTextSearchSorted() {
        String tenant = "base";
        create(tenant, new Fruit("Apricot"));
        create(tenant, new Fruit("Cherries"));
        create(tenant, new Fruit("Banana"));

        List<Fruit> sortedFruits = search(tenant, "*");
        List<String> fruitNames = sortedFruits.stream().map(Fruit::getName).collect(Collectors.toList());
        assertThat(fruitNames, is(List.of("Apricot", "Banana", "Cherries")));
        delete(tenant, sortedFruits);
    }

    @Test
    public void fullTextSearchCaseInsensitive() {
        String tenant = "base";
        String fruitName = "PiTaYa";
        create(tenant, new Fruit(fruitName));
        Fruit pitaya = search(tenant, fruitName.toLowerCase()).get(0);
        assertThat(pitaya.getName(), is(fruitName));
        delete(tenant, pitaya);
    }

    private void create(String tenantId, Fruit fruit) {
        getApp().given().with().body(fruit).contentType(ContentType.JSON)
                .when().post("/" + tenantId + "/fruits")
                .then()
                .statusCode(is(Response.Status.CREATED.getStatusCode()));
    }

    private void update(String tenantId, Fruit fruit) {
        getApp().given().with().body(fruit).contentType(ContentType.JSON)
                .when().put("/" + tenantId + "/fruits/" + fruit.getId())
                .then()
                .statusCode(is(Response.Status.OK.getStatusCode()));
    }

    private void delete(String tenantId, Fruit fruit) {
        getApp().given()
                .when().delete("/" + tenantId + "/fruits/" + fruit.getId())
                .then()
                .statusCode(is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    private void delete(String tenantId, List<Fruit> fruits) {
        fruits.stream().forEach(fruit -> delete(tenantId, fruit));
    }

    private List<Fruit> search(String tenantId, String terms) {
        io.restassured.response.Response response = given()
                .when().get("/" + tenantId + "/fruits/search?terms={terms}", terms);
        if (response.getStatusCode() == Response.Status.OK.getStatusCode()) {
            return response.as(FRUIT_LIST_TYPE_REF);
        }
        return EMPTY_LIST;
    }

    protected static String getElasticSearchConnectionChain(String host, int port) {
        return (host + ":" + port).replaceAll("http://", "");
    }

    protected abstract RestService getApp();
}
