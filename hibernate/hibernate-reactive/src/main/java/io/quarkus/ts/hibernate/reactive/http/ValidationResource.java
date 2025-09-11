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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.ts.hibernate.reactive.database.PersonEntity;
import io.smallrye.mutiny.Uni;

@Path("/validation")
public class ValidationResource {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;

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
        String query = switch (dbKind.toLowerCase()) {
            case "postgresql", "mssql" -> """
                    SELECT is_nullable, character_maximum_length
                    FROM information_schema.columns
                    WHERE table_name = 'person' AND column_name = 'name'
                    """;
            case "mariadb", "mysql" -> """
                    SELECT IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = 'person'
                      AND column_name = 'name'
                    """;
            case "oracle" -> """
                    SELECT nullable, char_length
                    FROM all_tab_columns
                    WHERE table_name = 'PERSON'
                      AND column_name = 'NAME'
                    """;
            default -> throw new IllegalStateException("Unsupported db-kind: " + dbKind);
        };

        return sessionFactory.withTransaction(session -> session.createNativeQuery(query)
                .getSingleResult())
                .map(result -> {
                    Object[] columns = (Object[]) result;
                    String isNullable = String.valueOf(columns[0]);

                    Integer maxLength = null;
                    if (columns[1] instanceof Number number) {
                        maxLength = number.intValue();
                    }

                    // For Oracle, nullable column returns "N" for NOT NULL and "Y" for NULL
                    // For other DBs, it returns "NO" for NOT NULL and "YES" for NULL
                    boolean notNullApplied = switch (isNullable.toUpperCase()) {
                        case "NO", "N" -> true;
                        default -> false;
                    };
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
