package io.quarkus.ts.http.graphql.tls;

import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi(configKey = "tls-client")
public interface TlsGraphQLClient {
    @Query
    String tlsHello();
}
