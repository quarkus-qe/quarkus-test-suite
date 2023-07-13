package io.quarkus.ts.spring.data.rest;

import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SpringDiIT {

    @QuarkusApplication
    public static final RestService app = new RestService()
            .withProperty("quarkus.hibernate-orm.active", "false");

    @Test
    public void testBeanExists() {
        app.given().get("/dependency-injection/bean-exists").then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testAccountServiceFromUserService() {
        app.given().get("/dependency-injection/user-service")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testPersonDaoFromSpringPersonService() {
        app.given().get("/dependency-injection/spring-person-service")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testCdiAndArcWay() {
        app.given().get("/dependency-injection/cdi-and-arc-way")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testCdiAndArcInstanceWay() {
        app.given().get("/dependency-injection/cdi-and-arc-instance-way")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testCdiAndArcStringWay() {
        app.given().get("/dependency-injection/cdi-and-arc-string-way")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    public void testBeanDeclaringClassMatch() {
        app.given().get("/dependency-injection/bean-declaring-class-match")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

}
