package io.quarkus.ts.hibernate.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.URILike;
import io.quarkus.ts.hibernate.search.fruit.Fruit;
import io.quarkus.ts.hibernate.search.fruit.FruitProducer;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

public abstract class AbstractMultitenantHibernateSearchIT {
    public static final TypeRef<List<Fruit>> FRUIT_LIST_TYPE_REF = new TypeRef<>() {
    };

    private static final List EMPTY_LIST = List.of();

    @LookupService
    static RestService app;

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
        sortedFruits = searchUsingMetamodelClass(tenant, "*");
        fruitNames = sortedFruits.stream().map(Fruit::getName).collect(Collectors.toList());
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
        pitaya = searchUsingMetamodelClass(tenant, fruitName.toLowerCase()).get(0);
        assertThat(pitaya.getName(), is(fruitName));
        delete(tenant, pitaya);
    }

    @Test
    public void testMetricAggregations() {
        String tenant = "base";
        var fruit = new Fruit();
        fruit.setName("apple");
        fruit.setPrice(1.5);
        create(tenant, fruit);
        var fruit2 = new Fruit();
        fruit2.setName("pear");
        fruit2.setPrice(5d);
        create(tenant, fruit2);
        var fruit3 = new Fruit();
        fruit3.setName("peach");
        fruit3.setPrice(3.5);
        create(tenant, fruit3);
        var fruit4 = new Fruit();
        fruit4.setName("raspberry");
        fruit4.setPrice(4.5);
        create(tenant, fruit4);
        var fruit5 = new Fruit();
        fruit5.setName("blueberry");
        fruit5.setPrice(5.5);
        create(tenant, fruit5);
        // all fruits
        app.given()
                .pathParam("tenantId", tenant)
                .queryParam("fetch", 5)
                .get("/{tenantId}/fruits/metric-aggregations")
                .then().statusCode(200)
                .body("numOfFetchedFruits", is(5))
                .body("averagePrice", is(4.0f));
        // average price for all fruits, only 3 are fetched: apple, blueberry, peach
        app.given()
                .pathParam("tenantId", tenant)
                .queryParam("fetch", 3)
                .get("/{tenantId}/fruits/metric-aggregations")
                .then().statusCode(200)
                .body("numOfFetchedFruits", is(3))
                .body("averagePrice", is(4.0f));
        // clean up after this test
        app.given()
                .pathParam("tenantId", tenant)
                .queryParam("min-price", 1.5)
                .queryParam("max-price", 5.5)
                .delete("/{tenantId}/fruits/delete-by-price-range")
                .then().statusCode(200)
                .body(is("5"));
    }

    @Test
    public void testProjectingMultivaluedFields() {
        // create a fruit
        String tenant = "base";
        var fruit1 = new Fruit();
        fruit1.setName("peach");
        var producer = new FruitProducer();
        producer.setName("Mark");
        fruit1.addProducer(producer);
        var producer2 = new FruitProducer();
        producer2.setName("Martin");
        fruit1.addProducer(producer2);
        var producer3 = new FruitProducer();
        producer3.setName("Luke");
        fruit1.addProducer(producer3);
        var persistedFruit = create(tenant, fruit1).extract().as(Fruit.class);
        int id = persistedFruit.getId();
        // verify the fruit is created correctly
        app.given()
                .pathParam("id", id)
                .pathParam("tenantId", tenant)
                .get("/{tenantId}/fruits/{id}")
                .then().statusCode(200)
                .body("name", is(fruit1.getName()))
                .body("producers", hasSize(3));
        // retrieve the fruit using a projection with multivalued field 'producerNames'
        app.given()
                .queryParam("fruit-name", fruit1.getName())
                .pathParam("tenantId", tenant)
                .get("/{tenantId}/fruits/projection-with-multivalued-field")
                .then().statusCode(200)
                .body("id", is(id))
                .body("name", is(fruit1.getName()))
                .body("producers", hasSize(3))
                .body("producers", Matchers.containsInAnyOrder("Mark", "Martin", "Luke"));
        delete(tenant, persistedFruit);
    }

    @Tag("QUARKUS-6545")
    @Test
    public void testRangeAggregations() {
        String tenant = "base";
        try {
            createFruit(tenant, "apple", 1.5);
            createFruit(tenant, "pear", 15d);
            createFruit(tenant, "peach", 13.5);
            createFruit(tenant, "raspberry", 24.5);
            createFruit(tenant, "blueberry", 35.5);
            createFruit(tenant, "strawberry", 50.45);

            // all fruits
            app.given()
                    .pathParam("tenantId", tenant)
                    .get("/{tenantId}/fruits/range-aggregations")
                    .then().statusCode(200)
                    .body("zeroToTen.avg", is(1))
                    .body("zeroToTen.min", is(1))
                    .body("zeroToTen.max", is(1))
                    .body("tenToTwenty.avg", is(14))
                    .body("tenToTwenty.min", is(13))
                    .body("tenToTwenty.max", is(15))
                    .body("twentyToInfinity.avg", is(36))
                    .body("twentyToInfinity.min", is(24))
                    .body("twentyToInfinity.max", is(50));
        } finally {
            // clean up after this test
            app.given()
                    .pathParam("tenantId", tenant)
                    .queryParam("min-price", 0.0)
                    .queryParam("max-price", 52.0)
                    .delete("/{tenantId}/fruits/delete-by-price-range")
                    .then().statusCode(200)
                    .body(is("6"));
        }
    }

    private void createFruit(String tenantId, String fruitName, double fruitPrice) {
        var fruit = new Fruit();
        fruit.setName(fruitName);
        fruit.setPrice(fruitPrice);
        create(tenantId, fruit);
    }

    private ValidatableResponse create(String tenantId, Fruit fruit) {
        return app.given().with().body(fruit).contentType(ContentType.JSON)
                .when().post("/" + tenantId + "/fruits")
                .then()
                .statusCode(is(Response.Status.CREATED.getStatusCode()));
    }

    private void update(String tenantId, Fruit fruit) {
        app.given().with().body(fruit).contentType(ContentType.JSON)
                .when().put("/" + tenantId + "/fruits/" + fruit.getId())
                .then()
                .statusCode(is(Response.Status.OK.getStatusCode()));
    }

    private void delete(String tenantId, Fruit fruit) {
        app.given()
                .when().delete("/" + tenantId + "/fruits/" + fruit.getId())
                .then()
                .statusCode(is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    private void delete(String tenantId, List<Fruit> fruits) {
        fruits.forEach(fruit -> delete(tenantId, fruit));
    }

    private List<Fruit> search(String tenantId, String terms) {
        io.restassured.response.Response response = app.given()
                .when().get("/" + tenantId + "/fruits/search?terms={terms}", terms);
        if (response.getStatusCode() == Response.Status.OK.getStatusCode()) {
            return response.as(FRUIT_LIST_TYPE_REF);
        }
        return EMPTY_LIST;
    }

    private List<Fruit> searchUsingMetamodelClass(String tenantId, String terms) {
        io.restassured.response.Response response = app.given()
                .when().get("/" + tenantId + "/fruits/search-using-metamodel-class?terms={terms}", terms);
        if (response.getStatusCode() == Response.Status.OK.getStatusCode()) {
            return response.as(FRUIT_LIST_TYPE_REF);
        }
        return List.of();
    }

    protected static String getElasticSearchConnectionChain(URILike uri) {
        return uri.toString().replaceAll("http://", "");
    }
}
