package io.quarkus.ts.sqldb.multiplepus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCustomDatasourceProducerIT {

    static final int MARIADB_PORT = 3306;
    static final int POSTGRESQL_PORT = 5432;

    @Test
    @Order(1)
    public void noActiveDatasource() {
        given()
                .get("/datasource-producer")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(containsString("No active datasource!"));

        given()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(not(containsString("agroal_max_used_count{datasource=")));
    }

    @Test
    @Order(2)
    public void postgresqlActive() {
        activateDeactivateDatasource(true, false);
        given()
                .get("/datasource-producer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("PostgreSQL"));

        checkMetricsIfDatasourceIsPresent("pg");
        checkMetricsIfDatasourceNotPresent("mariadb");
    }

    @Test
    @Order(3)
    public void mariadbActive() {
        activateDeactivateDatasource(false, true);
        given()
                .get("/datasource-producer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("MariaDB"));

        checkMetricsIfDatasourceIsPresent("mariadb");
        checkMetricsIfDatasourceNotPresent("pg");
    }

    @Test
    @Order(4)
    public void bothActiveDatasource() {
        activateDeactivateDatasource(true, true);
        // The custom datasource producer return postgres base on the condition logic, the metrics should show both active
        given()
                .get("/datasource-producer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("PostgreSQL"));

        checkMetricsIfDatasourceIsPresent("mariadb");
        checkMetricsIfDatasourceIsPresent("pg");
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

    public void activateDeactivateDatasource(boolean pgActive, boolean mariadbActive) {
        getApp().stop();
        getApp().withProperty("quarkus.datasource.\"pg\".active", String.valueOf(pgActive))
                .withProperty("quarkus.datasource.\"mariadb\".active", String.valueOf(mariadbActive))
                .start();
    }

    protected abstract RestService getApp();
}
