package io.quarkus.ts.hibernate.reactive.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.ts.hibernate.reactive.database.PersonEntity;
import io.smallrye.mutiny.Uni;

@Path("/validation")
public class ValidationResource {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @PUT
    @Path("/person/{name}")
    public Uni<Response> createPerson(String name) {
        return Panache.withTransaction(() -> PersonEntity.create(name))
                .map(nothing -> Response.status(Response.Status.CREATED).build())
                .onFailure()
                .recoverWithItem(e -> {
                    Throwable cause = e;
                    while (cause != null) {
                        String className = cause.getClass().getSimpleName();
                        if (ConstraintViolationException.class.getSimpleName().equals(className)) {
                            return Response.status(Response.Status.BAD_REQUEST).entity(cause.getMessage()).build();
                        }
                        cause = cause.getCause();
                    }
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @GET
    @Path("/insert-null-value")
    public Uni<String> insertNullValue() {
        return sessionFactory
                .withTransaction(s -> s.createNativeQuery("INSERT INTO person (name) VALUES (null)").executeUpdate())
                .onItemOrFailure()
                .transform((v, e) -> {
                    if (e instanceof ConstraintViolationException) {
                        return "NOT NULL constraint enforced";
                    }
                    return "Column allows NULL values";
                });
    }

    @GET
    @Path("/schema/person")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> validatePersonSchema() {
        return sessionFactory.withTransaction(session -> session.createNativeQuery(
                "SELECT is_nullable, character_maximum_length " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = 'person' AND column_name = 'name'")
                .getSingleResult())
                .map(result -> {
                    Object[] row = (Object[]) result;
                    String isNullable = (String) row[0];
                    Integer maxLength = (Integer) row[1];

                    boolean notNullApplied = "NO".equalsIgnoreCase(isNullable);
                    boolean correctMaxLength = Optional.ofNullable(maxLength)
                            .map(length -> length == PersonEntity.MAX_NAME_LENGTH)
                            .orElse(false);

                    Map<String, Object> columnValidationResult = new HashMap<>();
                    columnValidationResult.put("notNullApplied", notNullApplied);
                    columnValidationResult.put("correctMaxLength", correctMaxLength);

                    return Response.ok(columnValidationResult).build();
                });
    }
}
