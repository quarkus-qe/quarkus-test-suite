package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class HttpMinimumReactiveIT {

    private static final String fileName = "FLAKY";
    private RequestSpecification HTTP_CLIENT_SPEC = given();

    @Test
    public void httpServer() {
        givenSpec().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hello, World!"));
    }

    @Test
    @Tag("QUARKUS-2893")
    public void pathParamNameWithDash() throws IOException {
        // FIXME: remove this, I am just playing to understand flakiness reportng
        var filePath = Path.of("target").resolve(fileName);
        if (!Files.exists(filePath)) {
            filePath.toFile().createNewFile();
            Assertions.fail("failing to test flakiness reporting");
        }

        givenSpec().get("/api/hello/foo/AAAA").then().statusCode(HttpStatus.SC_NO_CONTENT);
        givenSpec().get("/api/hello/foo/AXY9").then().statusCode(HttpStatus.SC_NO_CONTENT);
        givenSpec().get("/api/hello/foo/ABCDFG").then().statusCode(HttpStatus.SC_NOT_FOUND);
        givenSpec().get("/api/hello/foo/abcd").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Tag("QUARKUS-1543")
    public void nativeListSerialization() {
        String teamId = (String) givenSpec().get("/api/cluster/default").then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getMap("clusters[0]")
                .get("team_id");

        assertEquals("qe", teamId);
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

    @Test
    public void shortRecordInResponseClass() {
        givenSpec().get("/api/hello/short-record-response").then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("data", is("ok"));
    }

    @Test
    public void shortRecordReturnedDirectly() {
        givenSpec().get("/api/hello/short-record").then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("data", is("ok"));
    }

    protected RequestSpecification givenSpec() {
        return HTTP_CLIENT_SPEC;
    }
}
