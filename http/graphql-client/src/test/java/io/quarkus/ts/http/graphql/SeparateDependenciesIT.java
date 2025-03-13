package io.quarkus.ts.http.graphql;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.graphql.client.ClientEndpoint;
import io.quarkus.ts.http.graphql.client.GraphQLClient;
import io.restassured.response.Response;

@QuarkusScenario
/*
 * https://github.com/smallrye/smallrye-graphql/issues/2271 can only be reproduced,
 * when there is only GraphQL *client* dependencies on the classpath.
 * This is why we need to have separate server and client apps with different dependencies
 */
public class SeparateDependenciesIT {
    @QuarkusApplication(classes = {
            DateEndpoint.class,
    }, dependencies = {
            @Dependency(artifactId = "quarkus-smallrye-graphql")
    })
    static final RestService server = new RestService();
    @QuarkusApplication(classes = {
            GraphQLClient.class,
            ClientEndpoint.class,
    })
    static final RestService client = new RestService()
            .withProperty("quarkus.smallrye-graphql-client.main.url",
                    () -> server.getURI(Protocol.HTTP).toString() + "/graphql");

    @Test
    @Tag("https://github.com/smallrye/smallrye-graphql/issues/2271")
    public void date() {
        Response response = client.given().basePath("/client/date").get();
        assertEquals(200, response.statusCode());
        assertEquals("2025-03-13T11:47:13+01:00", response.body().asString());
    }
}
