package io.quarkus.ts.http.graphql.client;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.graphql.Mutation;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi(configKey = "main")
public interface GraphQLClient {
    @Mutation("date")
    String processDate(OffsetDateTime date);
}
