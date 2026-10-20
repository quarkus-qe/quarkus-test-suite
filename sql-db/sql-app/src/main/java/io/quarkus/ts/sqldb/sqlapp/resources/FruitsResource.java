package io.quarkus.ts.sqldb.sqlapp.resources;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import io.quarkus.ts.sqldb.sqlapp.model.Fruit;

@Path("/fruits")
public class FruitsResource {

    public static final String TEXT_BLOCK_EXAMPLE = """
            JEP 378: Text Blocks
            Summary
            Add text blocks to the Java language. A text block is a multi-line string literal that avoids the need for
            most escape sequences, automatically formats the string in a predictable way, and gives the developer
            control over the format when desired.

            History
            Text blocks were proposed by JEP 355 in early 2019 as a follow-on to explorations begun in JEP 326
            (Raw String Literals), which was initially targeted to JDK 12 but eventually withdrawn and did not appear
             in that release. JEP 355 was targeted to JDK 13 in June 2019 as a preview feature. Feedback on JDK 13
             suggested that text blocks should be previewed again in JDK 14, with the addition of two new escape
             sequences. Consequently, JEP 368 was targeted to JDK 14 in November 2019 as a preview feature. Feedback
             on JDK 14 suggested that text blocks were ready to become final and permanent in JDK 15 with
             no further changes.
            """;

    @GET
    public List<Fruit> list() {
        return Fruit.getAll();
    }

    @POST
    @Transactional
    public Fruit add(@Valid Fruit fruit) {
        return fruit.save();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Fruit update(@Valid Fruit fruit, @PathParam("id") Long id) {
        if (!id.equals(fruit.id())) {
            throw new WebApplicationException("""
                        Try again baby!,
                        Terminator is watching you...
                    """, Response.Status.BAD_REQUEST.getStatusCode());
        }

        return fruit.update(id);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        if (!Fruit.deleteById(id)) {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("jpe_378")
    @Consumes(TEXT_PLAIN)
    public String simpleMultilineExample() {
        return TEXT_BLOCK_EXAMPLE;
    }

    @GET
    @Path("longestDesc")
    public Fruit longestDescription() {
        return Fruit.getLongestDescription();
    }
}
