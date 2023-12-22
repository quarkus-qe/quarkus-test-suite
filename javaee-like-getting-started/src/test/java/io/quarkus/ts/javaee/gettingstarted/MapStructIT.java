package io.quarkus.ts.javaee.gettingstarted;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;

@QuarkusScenario
public class MapStructIT {

    @Test
    public void testInjectionWithJakartaCdiComponentModel() {
        // tests:
        // - Jakarta CDI component model (mapper is @ApplicationScoped bean)
        // - mapping of beans to beans
        // - getters used for nested properties
        RestAssured
                .get("/mapper/injected-jakarta-cdi")
                .then()
                .statusCode(200)
                .body("greeting", Matchers.is("Hello Sebastian from Maximillian"));
    }

    @Test
    public void testInjectionWithJakartaComponentModel() {
        // tests:
        // - Jakarta component model (mappers are named beans)
        // - mapper decorator
        // - default interface method is used to convert types (FarewellPartOne -> FarewellPartOneDTO)
        // - @Mapping Java expression works
        // - using records
        // - injection of original (not decorated mapper) works as described in MapStruct documentation
        // - injection of other mappers inside decorator works
        // - @Mapping can use constant as value source
        RestAssured
                .given()
                .get("/mapper/injected-jakarta-happy-impl")
                .then()
                .statusCode(200)
                .body("partOne.data", Matchers.is("original_happy"))
                .body("partTwo", Matchers.is("1"));
        RestAssured
                .given()
                .get("/mapper/injected-jakarta-sad-impl")
                .then()
                .statusCode(200)
                .body("partOne.data", Matchers.is("original_sad"))
                .body("partTwo", Matchers.is("2"));
        RestAssured
                .given()
                .get("/mapper/injected-jakarta-default-method")
                .then()
                .statusCode(200)
                .body("partOne.data", Matchers.is("original_default"))
                .body("partTwo", Matchers.is("3"));
    }

    @Test
    public void testInjectionWithCdiComponentModel() {
        // tests:
        // - CDI component model (mapper is @ApplicationScoped bean)
        // - Object factory instantiates mapping target interface
        // - interface used as target mapping type
        // - mapper inherits configuration of Object factory and @ApplicationScoped bean (not a class annotated with @Mapper)
        // - mapper as abstract class
        // - @Mapping is not necessary for same-named properties
        // - mapping of enums
        RestAssured
                .given()
                .body("WEATHER")
                .post("/mapper/injected-cdi")
                .then()
                .statusCode(200)
                .body("topic", Matchers.is("BRITISH"));
        RestAssured
                .given()
                .body("TRUMP")
                .post("/mapper/injected-cdi")
                .then()
                .statusCode(200)
                .body("topic", Matchers.is("AMERICAN"));
    }

    @Test
    public void testInjectionWithDefaultComponentModel() {
        // tests:
        // - default component model (no dependency injection) so that we know static initialization works in native
        // - default provider builder (POJO is mapped to builder and the builder is passed to DTO constructor)
        // - @BeforeMapping and @AfterMapping callbacks are invoked
        // - @Mapping ignore option works (without that, an exception would be thrown for "ignored" property)
        // - @Mapping default value used when source property is null
        // - you can define mapping from property x to y and y is converted to target data type z with another map method
        RestAssured
                .given()
                .get("/mapper/no-cdi")
                .then()
                .statusCode(200)
                .body("data.innerData", Matchers.is("Ho Hey! How do you do? Enjoy yourself!"));
    }
}
