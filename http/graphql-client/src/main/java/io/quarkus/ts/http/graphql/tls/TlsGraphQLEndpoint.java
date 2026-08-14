package io.quarkus.ts.http.graphql.tls;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class TlsGraphQLEndpoint {
    @Query
    public String tlsHello() {
        return "Hello";
    }
}
