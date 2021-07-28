package io.quarkus.qe.sqldb.panacheflyway;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class DataSourceIT extends BaseIT {

    private static final String DATA_SOURCE_PATH = "/data-source";

    @Test
    public void shouldDataSourceBeProperlyConfigured() {
        dataSourcePath().get("/default/connection-provider-class")
                .then().statusCode(HttpStatus.SC_OK).and().body(is("com.mysql.cj.jdbc.Driver"));

        dataSourcePath().get("/with-xa/connection-provider-class")
                .then().statusCode(HttpStatus.SC_OK).and().body(is("com.mysql.cj.jdbc.MysqlXADataSource"));
    }

    private static final RequestSpecification dataSourcePath() {
        return given().accept(MediaType.APPLICATION_JSON).contentType(ContentType.JSON).when().basePath(DATA_SOURCE_PATH);
    }
}
