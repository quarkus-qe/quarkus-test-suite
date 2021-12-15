package io.quarkus.ts.reactive.http;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.ts.reactive.database.Author;
import io.quarkus.ts.reactive.database.AuthorRepository;
import io.quarkus.ts.reactive.database.Book;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PanacheEndpoint {

    @Inject
    AuthorRepository authors;

    @GET
    @Path("books")
    public Multi<String> all() {
        return Book.all()
                .map(Book::getTitle);
    }

    @GET
    @Path("books/{id}")
    public Uni<Response> find(Integer id) {
        return Book.byId(id)
                .map(book -> book == null
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok(book.getTitle()))
                .map(Response.ResponseBuilder::build);
    }

    @GET
    @Path("book")
    public Uni<Response> getConstant() {
        return Uni.createFrom().item(Response.ok("Slovník").build());
    }

    @GET
    @Path("isbn/{id}")
    public Uni<Response> findISBN(Integer id) {
        return Book.byId(id)
                .map(book -> book == null
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok(book.getISBN()))
                .map(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("isbn/{id}/{isbn}")
    public Uni<Response> changeISBN(Integer id, Long isbn) {
        return Book.byId(id)
                .flatMap(book -> {
                    book.setISBN(isbn);
                    return book.persistAndFlush();
                })
                .map(book -> book == null
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok())
                .map(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("books/{author}/{name}")
    public Uni<Response> createBook(Integer author, String name) {
        return Panache.withTransaction(() -> Book.create(author, name))
                .map(nothing -> Response.status(Response.Status.CREATED))
                .onFailure().recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()))
                .map(Response.ResponseBuilder::build);
    }

    @GET
    @Path("books/author/{name}")
    public Multi<String> search(String name) {
        return authors.findByName(name)
                .flatMap(Author::getBooks)
                .map(Book::getTitle);
    }

    @GET
    @Path("authors")
    // TODO: Return Json, when this will be fixed: https://github.com/quarkusio/quarkus/issues/18043
    public Multi<String> authors() {
        return authors.streamAll()
                .map(author -> author.getId() + " " + author.getName());
    }

    @GET
    @Path("author/{id}")
    public Uni<Response> author(Integer id) {
        return authors.findById(id)
                .map(author -> author == null
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok(author.getName()))
                .map(Response.ResponseBuilder::build);
    }

    @POST
    @Path("author/{name}")
    public Uni<Response> createAuthor(String name) {
        return authors.create(name)
                .map(ignored -> Response.status(Response.Status.CREATED))
                .onFailure().recoverWithItem(throwable -> {
                    return Response.status(Response.Status.BAD_REQUEST).entity(throwable.getMessage());
                })
                .map(Response.ResponseBuilder::build);
    }

    @DELETE
    @Path("author/{id}")
    public Uni<Response> deleteAuthor(Integer id) {
        return authors.deleteById(id)
                .call(isDeleted -> authors.flush())
                .map(isDeleted -> isDeleted
                        ? Response.status(Response.Status.NO_CONTENT)
                        : Response.status(Response.Status.NOT_FOUND))
                .map(Response.ResponseBuilder::build);
    }

    @GET
    @Path("dto/{id}")
    public Uni<Response> dtoQuery(Integer id) {
        return Book.find("author", id).project(BookDescription.class).list()
                .map(books -> books.isEmpty()
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok(books))
                .onFailure().recoverWithItem(error -> {
                    return Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage());
                })
                .map(Response.ResponseBuilder::build);
    }
}
