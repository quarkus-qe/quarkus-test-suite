package io.quarkus.ts.sqldb.sqlapp.resources;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import io.quarkus.ts.sqldb.sqlapp.model.Fruit;
import io.quarkus.ts.sqldb.sqlapp.model.FruitLookup;
import io.quarkus.ts.sqldb.sqlapp.storage.FruitEntity;

@Path("/fruits")
public class FruitsResource {

    public static final String TEXT_BLOCK_EXAMPLE = """
            Text Blocks
            Summary
            Add text blocks to the Java language. A text block is a multi-line string literal that avoids the need for
            most escape sequences, automatically formats the string in a predictable way, and gives the developer
            control over the format when desired.
            """;

    @GET
    @Path("/{id}")
    public Response find(@PathParam("id") Long id) {
        FruitEntity entity = FruitEntity.findById(id);
        FruitLookup result = (entity == null)
                ? new FruitLookup.NotFound(id)
                : new FruitLookup.Found(entity.toFruit());
        return Response.status(result.status()).entity(result.payload()).build();
    }

    @GET
    public List<Fruit> list() {
        return FruitEntity.<FruitEntity> find("#Fruits.findAllCustom")
                .stream()
                .map(FruitEntity::toFruit)
                .collect(Collectors.toList());
    }

    @POST
    @Transactional
    public Fruit add(@Valid Fruit fruit) {
        var entity = FruitEntity.from(fruit);
        FruitEntity.persist(entity);
        return entity.toFruit();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Fruit update(@Valid Fruit fruit, @PathParam("id") Long id) {
        if (!id.equals(fruit.id())) {
            throw new WebApplicationException("""
                        Path id must match the id in the request body
                    """, Response.Status.BAD_REQUEST.getStatusCode());
        }

        FruitEntity entity = FruitEntity.findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) {
            throw new NotFoundException();
        }

        entity.name = fruit.name();
        entity.description = fruit.description();

        return entity.toFruit();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        if (!FruitEntity.deleteById(id)) {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("text-block")
    @Produces(TEXT_PLAIN)
    public String simpleMultilineExample() {
        return TEXT_BLOCK_EXAMPLE;
    }

    @GET
    @Path("longest-description")
    public Fruit longestDescription() {
        return FruitEntity.getLongestDescription().toFruit();
    }
}
