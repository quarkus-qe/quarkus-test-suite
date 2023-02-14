package io.quarkus.ts.security.jwt;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.mutiny.Uni;

@GraphQLApi
public class GraphQLResource {

    @Inject
    JsonWebToken jwt;

    @Query("subject")
    public Uni<String> getSubject() {
        return Uni.createFrom().item(jwt.getSubject());
    }

}