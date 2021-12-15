package io.quarkus.ts.reactive.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.arc.Arc;
import io.quarkus.ts.reactive.database.Author;
import io.quarkus.ts.reactive.database.Book;
import io.smallrye.mutiny.Uni;

@Path("/hibernate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroundedEndpoint {

    Mutiny.SessionFactory factory;

    public GroundedEndpoint() {
        factory = Arc.container().instance(Mutiny.SessionFactory.class).get(); //TODO for some reason, @Inject fails. Need to investigate
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
