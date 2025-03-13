package io.quarkus.ts.http.graphql.client;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/client")
@Produces(MediaType.TEXT_PLAIN)
public class ClientEndpoint {

    @Inject
    GraphQLClient client;

    @GET
    @Path("/date")
    public String date() {
        OffsetDateTime dateOfWriting = OffsetDateTime.of(
                LocalDate.of(2025, Month.MARCH, 13),
                LocalTime.of(11, 47, 13),
                ZoneOffset.ofHours(1));
        return client.processDate(dateOfWriting);
    }
}
