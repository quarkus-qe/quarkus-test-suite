package io.quarkus.ts.http.jakartarest.reactive.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import io.quarkus.ts.http.jakartarest.reactive.model.Character;
import io.quarkus.ts.http.jakartarest.reactive.model.Hero;
import io.quarkus.ts.http.jakartarest.reactive.model.Villain;

@Path("/polymorphism")
public class PolymorphismResource {

    /**
     * Pattern matching for {@code switch} selects a branch by the runtime type of the deserialized
     * character and deconstructs each concrete record with a record pattern, binding its components
     * directly. {@code null} is handled as a dedicated label and the {@code switch} is exhaustive
     * over the sealed {@link Character} hierarchy, so no {@code default} branch is required.
     *
     * @param character polymorphic payload deserialized by Jackson
     * @return a description derived from the concrete character type
     */
    @POST
    public Response describe(Character character) {
        return switch (character) {
            case null -> throw new WebApplicationException("No character provided", Response.Status.BAD_REQUEST);
            case Hero(String name, Hero.Rank rank) -> Response.ok(name + " is a hero with rank " + rank).build();
            case Villain(String name, long bounty) -> Response.ok(name + " is a villain with bounty " + bounty).build();
        };
    }

    /**
     * Guarded patterns ({@code when}) let a single {@code switch} describe ranks either by a
     * condition or by a qualified enum constant.
     *
     * @param rankValue the rank name to describe
     * @return a description of the rank, or 404 if the rank is unknown
     */
    @GET
    @Path("/rank/{rank}")
    public Response describeRank(@PathParam("rank") String rankValue) {
        Hero.Rank rank;
        try {
            rank = Hero.Rank.valueOf(rankValue);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Unknown rank " + rankValue, Response.Status.NOT_FOUND);
        }
        return switch (rank) {
            case Hero.Rank r when r == Hero.Rank.S -> Response.ok("S rank is the highest rank a hero can achieve.").build();
            case Hero.Rank r when r == Hero.Rank.A -> Response.ok("A rank is for experienced heroes.").build();
            case Hero.Rank.B -> Response.ok("B rank is for heroes with a few jobs done.").build();
            case Hero.Rank.F -> Response.ok("F rank is for heroes who just started.").build();
            default -> throw new IllegalStateException("Unexpected rank " + rank);
        };
    }
}
