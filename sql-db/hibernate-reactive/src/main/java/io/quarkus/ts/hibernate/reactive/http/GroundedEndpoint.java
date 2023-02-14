package io.quarkus.ts.hibernate.reactive.http;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.arc.Arc;
import io.quarkus.ts.hibernate.reactive.database.Author;
import io.quarkus.ts.hibernate.reactive.database.Book;
import io.smallrye.mutiny.Uni;

@Path("/hibernate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroundedEndpoint {

    Mutiny.SessionFactory factory;

    @Inject
    @RestClient
    SomeApi someApi;

    public GroundedEndpoint() {
        // We are not injecting a sessionFactory because we have more than one un-named datasources
        factory = Arc.container().instance(Mutiny.SessionFactory.class).get();
    }

    @GET
    @Path("books/{id}")
    public Uni<Response> getBooksThroughSession(Integer id) {
        return factory.openSession()
                .flatMap(session -> session.find(Book.class, id)
                        .map(book -> book == null
                                ? Response.status(Response.Status.NOT_FOUND)
                                : Response.ok(book.getTitle()))
                        .map(Response.ResponseBuilder::build)
                        .eventually(session::close));
    }

    @GET
    @Path("books/author/{authorId}")
    public Uni<Response> findFirstBooksByAuthor(Integer authorId) {
        return factory.withSession(session -> {
            return session.createNativeQuery("Select * from books where author='" + authorId + "'", Book.class)
                    .setMaxResults(1)
                    .getResultList()
                    .map(books -> Response.ok(books).build());
        });
    }

    @GET
    @Path("books/starts_with/{prefix}")
    public Uni<Response> findByPrefix(String prefix) {
        return factory.withSession(session -> {
            return session.createNamedQuery("find_by_title_prefix", Book.class)
                    .setParameter("prefix", prefix + "%")
                    .getResultList()
                    .map(books -> Response.ok(books).build());
        });
    }

    @POST
    @Path("author/create/{name}")
    public Uni<Response> createStateless(String name) {
        return factory.openStatelessSession().flatMap(session -> {
            Author author = new Author();
            author.setName(name);
            return session.insert(author);
        })
                .map(ignored -> Response.status(Response.Status.CREATED))
                .map(Response.ResponseBuilder::build);
    }

    @POST
    @Path("books/{authorName}/{name}")
    public Uni<Response> createBook(String authorName, String name) {
        return factory.withTransaction((session, transaction) -> {
            Author author = new Author();
            author.setName(authorName);
            return session.persist(author)
                    .map(nothing -> session.getReference(author).getId())
                    .map(authorId -> {
                        Book book = new Book();
                        book.setAuthor(authorId);
                        book.setTitle(name);
                        return book;
                    })
                    .flatMap(session::persist);
        })
                .map(nothing -> Response.status(Response.Status.CREATED))
                .onFailure().recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST).entity(error.getMessage()))
                .map(Response.ResponseBuilder::build);
    }

    // https://github.com/quarkusio/quarkus/issues/18977
    @GET
    @Path("bookThroughSession/{authorName}/{name}")
    public Uni<Author> createBookThroughSession(String authorName, String name) {
        Author author = new Author();
        author.setName(authorName);
        return factory.withTransaction(
                (session1, transaction) -> factory.withSession(session2 -> session1.persist(author).chain(session1::flush))
                        .onItem().call(() -> someApi.doSomething())
                        .onItem().transformToUni((ignore -> factory.withSession(session3 -> {
                            Book book = new Book();
                            book.setAuthor(author.getId());
                            book.setTitle(name);
                            return session3.persist(book);
                        }))).onItem()
                        .transformToUni(ignore -> factory.withSession(session3 -> session3.find(Author.class, author.getId())))
                        .onFailure()
                        .transform(error -> new WebApplicationException(error.getMessage(), Response.Status.BAD_REQUEST)));
    }

    @GET
    @Path("isbn/{id}")
    public Uni<Response> getRawISBN(Integer id) {
        return factory.withSession(session -> {
            return session.createNativeQuery("SELECT isbn FROM books WHERE id = " + id, String.class)
                    .getSingleResult()
                    .map(books -> Response.ok(books).build());
        });
    }
}
