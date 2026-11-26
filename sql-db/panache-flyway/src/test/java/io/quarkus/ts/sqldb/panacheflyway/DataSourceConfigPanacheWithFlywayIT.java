package io.quarkus.ts.sqldb.panacheflyway;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
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
