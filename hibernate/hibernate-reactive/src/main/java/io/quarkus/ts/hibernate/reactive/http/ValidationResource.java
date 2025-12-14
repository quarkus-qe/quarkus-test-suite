package io.quarkus.ts.hibernate.reactive.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.ts.hibernate.reactive.database.BookCollectionLegacyValid;
import io.quarkus.ts.hibernate.reactive.database.BookCollectionTypeUseValid;
import io.quarkus.ts.hibernate.reactive.database.ClientDevice;
import io.quarkus.ts.hibernate.reactive.database.LibraryAuthor;
import io.quarkus.ts.hibernate.reactive.database.LibraryBook;
import io.quarkus.ts.hibernate.reactive.database.PersonEntity;
import io.quarkus.ts.hibernate.reactive.database.XmlValidatedCustomer;
import io.smallrye.mutiny.Uni;

@Path("/validation")
public class ValidationResource {

    @Inject
    Validator validator;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;

    @PUT
    @Path("/person/{name}")
    public Uni<Response> createPerson(String name) {
        return Panache.withTransaction(() -> PersonEntity.create(name))
                .replaceWith(() -> Response.status(Response.Status.CREATED).build())
                .onFailure()
                .recoverWithItem(e -> {
                    Throwable cause = e;
                    while (cause != null) {
                        if (cause instanceof jakarta.validation.ConstraintViolationException) {
                            return Response.status(Response.Status.BAD_REQUEST).entity(cause.getMessage()).build();
                        }
                        cause = cause.getCause();
                    }
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @PUT
    @Path("/xml/customer/{name}/{email}")
    public Uni<Response> createXmlCustomer(@PathParam("name") String name, @PathParam("email") String email) {
        XmlValidatedCustomer customer = new XmlValidatedCustomer(name, email);

        return sessionFactory.withTransaction(session -> session.persist(customer))
                .replaceWith(() -> Response.status(Response.Status.CREATED).build())
                .onFailure()
                .recoverWithItem(e -> {
                    Throwable cause = e;
                    while (cause != null) {
                        if (cause instanceof jakarta.validation.ConstraintViolationException) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity("XML constraint violation: " + cause.getMessage()).build();
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
                .transform((v, cause) -> {
                    if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
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

    @PUT
    @Path("/author/{name}/{title}")
    public Uni<Response> validateAuthor(String name, String title) {
        return Panache.withTransaction(() -> {
            LibraryAuthor author = new LibraryAuthor();
            author.name = name;

            LibraryBook book = new LibraryBook();
            book.title = title;

            book.author = author;
            author.books.add(book);

            return author.persistAndFlush();
        })
                .map(n -> Response.status(Response.Status.CREATED))
                .onFailure().recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()))
                .map(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("/container/{mode}/{title}")
    public Uni<Response> validateContainer(String mode, String title) {
        return Panache.withTransaction(() -> {

            LibraryBook book = new LibraryBook();
            book.title = title;

            Object dto = mode.equals("legacy")
                    ? new BookCollectionLegacyValid(List.of(book))
                    : new BookCollectionTypeUseValid(List.of(book));

            Set<ConstraintViolation<Object>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return Uni.createFrom().nullItem();
        })
                .map(n -> Response.status(Response.Status.CREATED))
                .onFailure().recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()))
                .map(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("/device/{type}/{ip}")
    public Uni<Response> createDevice(String type, String ip) {
        return Panache.withTransaction(() -> {
            ClientDevice device = new ClientDevice();
            switch (type) {
                case "ipv4" -> device.ipv4 = ip;
                case "ipv6" -> device.ipv6 = ip;
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            }

            return device.persistAndFlush();
        })
                .map(n -> Response.status(Response.Status.CREATED))
                .onFailure().recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()))
                .map(Response.ResponseBuilder::build);
    }
}
