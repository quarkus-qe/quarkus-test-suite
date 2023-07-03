package io.quarkus.ts.infinispan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class InfinispanIT {
    @Container(image = "${infinispan.image}", port = 11222)
    static InfinispanService infinispan = new InfinispanService()
            .withUsername("admin")
            .withPassword("password");

    @QuarkusApplication()
    static RestService service = new RestService()
            .withProperty("quarkus.infinispan-client.hosts",
                    () -> infinispan.getURI().toString())
            .withProperties("infinispan-it.properties");

    @Test
    void smoke() {
        String firstCache = service.given()
                .get("/first-counter/get-cache")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();
        String secondCache = service.given()
                .get("/second-counter/get-cache")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEquals(firstCache, secondCache);
    }
}
