package io.quarkus.ts.sqldb.sqlapp;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@Tag("QUARKUS-6164")
@QuarkusTest
@WithTestResource(value = MySQLTestResourceLifecycleManager.class, scope = TestResourceScope.RESTRICTED_TO_CLASS)
public class DriverRegistrationSecondGroupTest {

    @Test
    public void verifyDriversStillRegistered() {
        Response response = given()
                .when()
                .get("/drivers/list")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String drivers = response.asString();
        assertTrue(drivers.contains("io.quarkus.ts.sqldb.sqlapp.driver.TestJdbcDriver"),
                "Custom test driver should still be registered in second group");
    }

}
