package io.quarkus.ts.http.advanced.reactive;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;

@Path("/persons")
public class PersonResource {

    private final Set<Person> personSet = Collections.synchronizedSet(new LinkedHashSet<>());

    @Path("/process-json")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> processJson(String jsonData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Set<Person> persons = objectMapper.readValue(jsonData, new TypeReference<Set<Person>>() {
            });
            for (Person person : persons) {
                if (person == null || person.getName().isEmpty()) {
                    return Uni.createFrom().item(Response.status(400).entity("Some fields are empty or null").build());
                } else {
                    personSet.add(person);
                }
            }
            return Uni.createFrom().item(() -> Response.status(201).entity(personSet).build());

        } catch (JsonMappingException ex) {
            return Uni.createFrom()
                    .item(Response.status(400).entity("JsonMappingException : " + ex.getMessage()).build());
        } catch (JsonProcessingException e) {
            return Uni.createFrom().item(
                    Response.status(400).entity("Invalid JSON - JsonProcessingException : " + e.getOriginalMessage()).build());
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAllPersons() {
        if (personSet.isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        } else {
            return Uni.createFrom().item(Response.ok(personSet).build());
        }
    }
}
