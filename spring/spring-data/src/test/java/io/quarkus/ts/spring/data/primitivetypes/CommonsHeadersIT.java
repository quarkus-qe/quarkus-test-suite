package io.quarkus.ts.spring.data.primitivetypes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.data.AbstractDbIT;
import io.restassured.http.Headers;

@QuarkusScenario
public class CommonsHeadersIT extends AbstractDbIT {
    //This is for regression test for https://github.com/quarkusio/quarkus/pull/12234
    @Test
    @DisplayName("prototype scope: x-count header must be equal to request amount")
    void testRequestAmount() {
        Set<String> xCount = new HashSet<>();

        for (int index = 0; index < 3; index++) {
            Headers headers = app.given().get("/cat/customFindDistinctivePrimitive/2").headers();
            xCount.addAll(headers.getValues("x-count"));
        }

        assertThat("Unexpected x-count header value(Spring Prototype Scope). Must be the same as HTTP request amount.",
                xCount.size(), is(3));
    }

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/12234
    @Test
    @DisplayName("Singleton scope: x-instance-id header must be the same")
    void testInstanceId() {
        Set<String> instanceIds = new HashSet<>();
        for (int index = 0; index < 3; index++) {
            Headers headers = app.given().get("/cat/customFindDistinctivePrimitive/2").headers();
            instanceIds.addAll(headers.getValues("x-instance"));
        }

        assertThat("Unexpected x-instance header value(Spring Singleton Scope). Must be 1", instanceIds.size(), is(1));
    }

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/12234
    @Test
    @DisplayName("request scope: x-request header must be equal to request amount")
    public void testRequestScope() {
        Set<String> requestIds = new HashSet<>();
        for (int index = 0; index < 3; index++) {
            Headers headers = app.given().get("/cat/customFindDistinctivePrimitive/2").headers();
            requestIds.addAll(headers.getValues("x-request"));
        }

        assertThat("Unexpected x-request header value(Spring Request Scope). Must be the same as HTTP request amount.",
                requestIds.size(), is(3));
    }

}
