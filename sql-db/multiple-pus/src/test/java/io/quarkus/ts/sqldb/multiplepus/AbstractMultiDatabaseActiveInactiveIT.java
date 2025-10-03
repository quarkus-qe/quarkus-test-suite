package io.quarkus.ts.sqldb.multiplepus;

import static io.quarkus.ts.sqldb.multiplepus.FungiTenantResolver.TENANT_HEADER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.sqldb.multiplepus.model.fruit.Fruit;
import io.quarkus.ts.sqldb.multiplepus.model.fungus.Fungus;
import io.quarkus.ts.sqldb.multiplepus.model.vegetable.Vegetable;
import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractMultiDatabaseActiveInactiveIT {

    static final int MARIADB_PORT = 3306;
    static final int POSTGRESQL_PORT = 5432;

    public static final String INACTIVE_DATASOURCE_ERROR = "Cannot retrieve the EntityManagerFactory/SessionFactory";

    @Test
    @Order(1)
    @DisplayName("Check that all named data sources are inactive")
    public void noActiveDatasource() {
        checkFruitIsInactive();
        checkVegetableIsInactive();
        checkFungusIsInactive();
    }

    @Test
    @Order(2)
    @DisplayName("Check datasource metrics is not present when all datasources are inactive")
    public void noActiveDatasourceCheckMetrics() {
        given()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(not(containsString("agroal_max_used_count{datasource=")));
    }

    @Test
    @Order(3)
    @DisplayName("Check that fruit datasource is active and others are inactive")
    public void fruitActive() {
        activateDeactivateDatasource(true, false, false);

        given().get("/fruit")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(7));

        checkVegetableIsInactive();
        checkFungusIsInactive();
    }

    @Test
    @Order(4)
    @DisplayName("Activated datasource (fruit) should be able to make operation")
    public void createAndDeleteFruit() {
        Fruit fruit = new Fruit();
        fruit.name = "Canteloupe";

        int latestFruitId = createRequest(fruit, fruit.name, "/fruit");
        getByIdRequest(fruit.name, "/fruit/" + latestFruitId);
        deleteByIdRequest("/fruit/" + latestFruitId);
    }

    @Test
    @Order(5)
    @DisplayName("Check datasource metrics is present only for active datasource (fruits)")
    public void fruitActiveCheckMetrics() {
        checkMetricsIfDatasourceIsPresent("fruits");
        checkMetricsIfDatasourceNotPresent("vegetables");
    }

    @Test
    @Order(6)
    @DisplayName("Check that fruit datasource is inactive and others are active")
    public void vegetableAndFungiActive() {
        activateDeactivateDatasource(false, true, true);

        checkFruitIsInactive();

        given().get("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("", hasSize(7));

        given().get("/fungus")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(Matchers.is("4"));
    }

    @Test
    @Order(7)
    @DisplayName("Activated datasource (vegetable) should be able to make operation")
    public void createAndDeleteVegetable() {
        Vegetable vegetable = new Vegetable();
        vegetable.name = "Eggplant";

        int latestVegetableId = createRequest(vegetable, vegetable.name, "/vegetable");
        getByIdRequest(vegetable.name, "/vegetable/" + latestVegetableId);
        deleteByIdRequest("/vegetable/" + latestVegetableId);
    }

    @Test
    @Order(8)
    @DisplayName("Activated persistence unit name (fungus) should be able to make operation on tenant")
    public void createAndDeleteFungus() {
        Fungus fungus = new Fungus();
        fungus.name = "W. macrospora";

        Map<String, String> headers = Map.of(TENANT_HEADER, "Wynnea");

        int latestFungusId = createRequest(fungus, fungus.name, "/fungus", headers);
        getByIdRequest(fungus.name, "/fungus/" + latestFungusId, headers);
        deleteByIdRequest("/fungus/" + latestFungusId, headers);
    }

    @Test
    @Order(9)
    @DisplayName("Check datasource metrics is present only for active datasource (vegatables)")
    public void vegetableActiveCheckMetrics() {
        checkMetricsIfDatasourceIsPresent("vegetables");
        checkMetricsIfDatasourceNotPresent("fruits");
    }

    @DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
    @Tag("QUARKUS-6521")
    @Test
    @Order(10)
    @DisplayName("Check the active datasource can be used from GraphQL endpoint running on a virtual thread")
    public void testGraphQLWithVirtualThreadsAndPostgreSQL() {
        Integer latestVegetableId = null;
        try {
            Vegetable vegetable = new Vegetable();
            vegetable.name = "Onion";

            latestVegetableId = getApp()
                    .given()
                    .basePath("graphql")
                    .contentType("application/json")
                    .body("""
                            {
                                "query": "mutation { vegetable(name:\\"%s\\") { id } }"
                            }
                            """.formatted(vegetable.name))
                    .post()
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().jsonPath().getInt("data.vegetable.id");

            getApp()
                    .given()
                    .basePath("graphql")
                    .contentType("application/json")
                    .body("""
                            {
                                "query": "{ vegetable(id:%d) { name } }"
                            }
                            """.formatted(latestVegetableId))
                    .post()
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("data.vegetable.name", is(vegetable.name));

            getApp()
                    .given()
                    .get("/q/metrics")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(containsString("jvm_threads_virtual_pinned_seconds_sum 0.0"));
        } finally {
            if (latestVegetableId != null) {
                deleteByIdRequest("/vegetable/" + latestVegetableId);
            }
        }
    }

    public int createRequest(PanacheEntity edibles, String name, String path, Map<String, String> headers) {
        return given()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(edibles)
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", equalTo(name))
                .extract().path("id");
    }

    public int createRequest(PanacheEntity edibles, String name, String path) {
        return createRequest(edibles, name, path, Map.of());
    }

    public void getByIdRequest(String name, String path, Map<String, String> headers) {
        given().headers(headers)
                .get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(name));
    }

    public void getByIdRequest(String name, String path) {
        getByIdRequest(name, path, Map.of());
    }

    public void deleteByIdRequest(String path, Map<String, String> headers) {
        given().headers(headers)
                .delete(path)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    public void deleteByIdRequest(String path) {
        deleteByIdRequest(path, Map.of());
    }

    public void checkFruitIsInactive() {
        given().get("/fruit")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(containsString(INACTIVE_DATASOURCE_ERROR));
    }

    public void checkVegetableIsInactive() {
        given().get("/vegetable")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(containsString(INACTIVE_DATASOURCE_ERROR));
    }

    public void checkFungusIsInactive() {
        given().get("/fungus")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(containsString(INACTIVE_DATASOURCE_ERROR));
    }

    /**
     * Check if the metrics contains active datasource
     */
    public void checkMetricsIfDatasourceIsPresent(String datasourceName) {
        given()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("agroal_max_used_count{datasource=\"" + datasourceName + "\"}"));
    }

    /**
     * Check if the metrics doesn't contain inactive datasource
     */
    public void checkMetricsIfDatasourceNotPresent(String datasourceName) {
        given()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(not(containsString("agroal_max_used_count{datasource=\"" + datasourceName + "\"}")));
    }

    public void activateDeactivateDatasource(boolean fruitActivate, boolean vegetableActivate, boolean fungiActivate) {
        getApp().stop();
        getApp().withProperty("quarkus.datasource.\"fruits\".active", String.valueOf(fruitActivate))
                .withProperty("quarkus.datasource.\"vegetables\".active", String.valueOf(vegetableActivate || fungiActivate))
                .withProperty("quarkus.hibernate-orm.\"fruits\".active", String.valueOf(fruitActivate))
                .withProperty("quarkus.hibernate-orm.\"vegetables\".active", String.valueOf(vegetableActivate))
                .withProperty("quarkus.hibernate-orm.\"fungi\".active", String.valueOf(fungiActivate))
                .start();
    }

    protected abstract RestService getApp();
}
