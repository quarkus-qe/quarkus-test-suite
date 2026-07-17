package io.quarkus.ts.vertx;

import static org.hamcrest.CoreMatchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-8412") // beware, that this issues is about compilation failure
@DisabledOnNative(reason = "Support for Hazelcast in native mode is not implemented: https://github.com/quarkusio/quarkus/issues/9877")
public class HazelcastIT {

    @QuarkusApplication(dependencies = @Dependency(groupId = "io.vertx", artifactId = "vertx-hazelcast"))
    static RestService app = new RestService();

    @Test
    public void smoke() {
        app.given().get("/hello?name=you").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, you!"));
    }
}
