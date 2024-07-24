package io.quarkus.ts.http.advanced.reactive;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> uploadFootballJson(String data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Set<FootballTeam> teamSet = objectMapper.readValue(data, new TypeReference<Set<FootballTeam>>() {
            });
            for (FootballTeam footballTeam : teamSet) {
                if (footballTeam == null || footballTeam.getName().isEmpty()) {
                    return Uni.createFrom().item(Response.status(400).entity("Some fields are empty or null").build());
                } else {
                    footballTeamSet.add(footballTeam);
                }
            }
            return Uni.createFrom()
                    .item(() -> {
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("teams", footballTeamSet);
                        responseMap.put("message", "Teams added successfully");
                        return Response.status(201).entity(responseMap).build();
                    });
        } catch (JsonMappingException ex) {
            LOG.error(ex.getMessage());
            return Uni.createFrom()
                    .item(Response.status(400).entity("JsonMappingException : " + ex.getMessage()).build());
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
            return Uni.createFrom().item(
                    Response.status(400).entity("Invalid JSON - JsonProcessingException : " + e.getOriginalMessage()).build());
        }

    }

    @DELETE
    @Path("/clear")
    public Response clearTeams() {
        footballTeamSet.clear();
        return Response.ok().build();
    }

}
