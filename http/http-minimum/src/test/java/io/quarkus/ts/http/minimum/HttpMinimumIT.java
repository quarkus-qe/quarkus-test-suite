package io.quarkus.ts.http.minimum;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class HttpMinimumIT {

    private RequestSpecification HTTP_CLIENT_SPEC = given();

    @Test
    public void httpServer() {
        givenSpec().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content", is("Hello, World!"));
    }

    @Test
    @Tag("QUARKUS-2893")
    public void pathParamNameWithDash() {
        givenSpec().get("/api/hello/foo/AAAA").then().statusCode(HttpStatus.SC_NO_CONTENT);
        givenSpec().get("/api/hello/foo/AXY9").then().statusCode(HttpStatus.SC_NO_CONTENT);
        givenSpec().get("/api/hello/foo/ABCDFG").then().statusCode(HttpStatus.SC_NOT_FOUND);
        givenSpec().get("/api/hello/foo/abcd").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Tag("QUARKUS-2754")
    public void transferEncodingAndContentLengthHeadersAreNotAllowedTogether() {
        givenSpec().get("/api/hello/no-content-length").then()
                .statusCode(HttpStatus.SC_OK)
                .headers("Transfer-Encoding", "chunked")
                .headers("Content-Length", is(nullValue()));

        givenSpec().get("/api/hello/json").then()
                .statusCode(HttpStatus.SC_OK)
                .headers("Transfer-Encoding", is(nullValue()))
                .headers("Content-Length", is(notNullValue()));
    }

    protected RequestSpecification givenSpec() {
        return HTTP_CLIENT_SPEC;
    }
}
