package io.quarkus.ts.http.graphql;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class DateEndpoint {

    @Mutation("date")
    public String processDate(OffsetDateTime date) {
        return date.toString();
    }

    @Query("error")
    /**
     * GraphQL endpoint has to have at least one query (as opposed to mutations), otherwise it is an error
     * See these for details:
     * - https://github.com/quarkusio/quarkus/discussions/25434
     * - https://spec.graphql.org/June2018/#sec-Root-Operation-Types
     */
    public String throwException() throws IllegalStateException {
        throw new IllegalStateException("This endpoint should not be called!");
    }
}
