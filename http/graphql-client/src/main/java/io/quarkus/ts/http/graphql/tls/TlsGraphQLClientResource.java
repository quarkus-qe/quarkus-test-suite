package io.quarkus.ts.http.graphql.tls;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;

@Path("/client/tls")
@ApplicationScoped
public class TlsGraphQLClientResource {
    @Inject
    TlsGraphQLClient typesafeClient;

    @Inject
    @GraphQLClient("tls-dynamic")
    DynamicGraphQLClient dynamicClient;

    @GET
    @Path("/typesafe")
    public String callTypesafeClient() {
        try {
            return typesafeClient.tlsHello();
        } catch (Exception e) {
            throw new WebApplicationException("Typesafe GraphQL TLS request failed", 502);
        }
    }

    @GET
    @Path("/dynamic")
    public String callDynamicClient() {
        try {
            Response response = dynamicClient.executeSync("{ tlsHello }");
            return response.getData().getString("tlsHello");
        } catch (Exception e) {
            throw new WebApplicationException("Dynamic GraphQL TLS request failed", 502);
        }
    }
}
