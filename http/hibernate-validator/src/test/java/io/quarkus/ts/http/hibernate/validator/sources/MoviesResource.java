package io.quarkus.ts.http.hibernate.validator.sources;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/movies")
public class MoviesResource {

    public record Director(
            @NotBlank(message = "Name may not be blank") @Size(min = 1, max = 20) String name,
            @NotBlank(message = "Surname may not be blank") @Size(max = 20) String surname) {
    }

    public record Movie(
            @NotBlank(message = "ID may not be blank") String id,
            @Valid Director director,
            Boolean released) {
    }

    private List<Movie> movies = List.of(
            new Movie("1", new Director("John", "Wick"), true),
            new Movie("2", new Director("Mary", "Poppins"), false),
            new Movie("3", new Director("Jack", "Sparrow"), true),
            new Movie("4", null, false));

    @GET
    public List<Movie> getMovies() {
        return movies;
    }

    @GET
    @Path("/{id}")
    public Movie getMovie(@PathParam("id") String id) {
        return movies
                .stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    @POST
    public Movie addMovie(@Valid Movie movie) {
        return movie;
    }
}
