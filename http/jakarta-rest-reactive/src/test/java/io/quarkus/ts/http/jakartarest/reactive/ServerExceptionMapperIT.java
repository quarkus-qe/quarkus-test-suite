package io.quarkus.ts.http.jakartarest.reactive;

import static io.quarkus.ts.http.jakartarest.reactive.exceptions.ServerExceptionMapperResource.EXCEPTION_MAPPER_PATH;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;

@Tag("QUARKUS-7728")
@QuarkusScenario
public class ServerExceptionMapperIT {

    @Test
    public void verifyGenericExceptionMapperWorks() {
        RestAssured.get(EXCEPTION_MAPPER_PATH + "/generic")
                .then().statusCode(499);
    }

}
