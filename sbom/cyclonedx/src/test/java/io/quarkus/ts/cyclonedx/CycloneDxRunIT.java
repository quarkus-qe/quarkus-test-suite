package io.quarkus.ts.cyclonedx;

import static io.quarkus.ts.cyclonedx.CycloneDxUtils.assertEmbeddedSbomComponents;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.io.InputStream;

import org.cyclonedx.parsers.JsonParser;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class CycloneDxRunIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void testApplicationEndpoint() {
        given().get("/hello").then().statusCode(200).body("content", is("Hello, World!"));
    }

    @Test
    public void testEndpointServesEmbeddedSbom() throws Exception {
        InputStream is = given().get("/.well-known/sbom").then().statusCode(200).extract().body().asInputStream();
        assertEmbeddedSbomComponents(new JsonParser().parse(is));
    }
}
