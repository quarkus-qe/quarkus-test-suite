package io.quarkus.ts.sqldb.sqlapp;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@Tag("QUARKUS-6164")
@QuarkusTest
@WithTestResource(value = MySQLTestResourceLifecycleManager.class, scope = TestResourceScope.MATCHING_RESOURCES)
public class DriverRegistrationFirstGroupTest {

    @Test
    public void firstGroupVerifyDrivers() {
        String drivers = given()
                .when()
                .get("/drivers/list")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertTrue(drivers.contains("io.quarkus.ts.sqldb.sqlapp.driver.TestJdbcDriver"),
                "Custom test driver should be registered in first group");
    }

}
