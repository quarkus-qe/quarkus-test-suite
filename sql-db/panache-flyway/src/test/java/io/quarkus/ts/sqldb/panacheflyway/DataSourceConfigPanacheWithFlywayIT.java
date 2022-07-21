package io.quarkus.ts.sqldb.panacheflyway;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.specification.RequestSpecification;

// TODO https://github.com/quarkus-qe/quarkus-test-suite/issues/756
@Tag("fips-incompatible") //native-mode
@QuarkusScenario
public class DataSourceConfigPanacheWithFlywayIT extends PanacheWithFlywayBaseIT {

    private static final String DATA_SOURCE_PATH = "/data-source";

    @Test
    public void shouldDataSourceBeProperlyConfigured() {
        dataSourcePath().get("/default/connection-provider-class")
                .then().statusCode(HttpStatus.SC_OK).and().body(is("com.mysql.cj.jdbc.Driver"));

        dataSourcePath().get("/with-xa/connection-provider-class")
                .then().statusCode(HttpStatus.SC_OK).and().body(is("com.mysql.cj.jdbc.MysqlXADataSource"));
    }

    private static final RequestSpecification dataSourcePath() {
        return given().when().basePath(DATA_SOURCE_PATH);
    }
}
