package io.quarkus.ts.http.advanced.reactive;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;

/**
 *
 * When a JSON extension is installed such as quarkus-rest-jackson or quarkus-rest-jsonb
 * Quarkus will use the application/json media type by default for most return values
 */
@Path("/football")
public class FootballTeamResource {

    private static final Logger LOG = Logger.getLogger(FootballTeamResource.class);

    private Set<FootballTeam> footballTeamSet = Collections.synchronizedSet(new LinkedHashSet<>());

    @POST
    @Path("/upload-football-json")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> uploadFootballTeams(List<FootballTeam> footballTeams) {
        footballTeamSet.addAll(footballTeams);
        return Uni.createFrom()
                .item(() -> {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("teams", footballTeamSet);
                    responseMap.put("message", "Teams added successfully");
                    return Response.status(201).entity(responseMap).build();
                });
    }

    @DELETE
    @Path("/clear")
    public Response clearTeams() {
        footballTeamSet.clear();
        return Response.ok().build();
    }

}
