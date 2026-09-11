package io.quarkus.ts.hibernate.reactive.http;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.ts.hibernate.reactive.database.Book;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

@Path("/transactional")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionalEndpoint {

    private static final ConcurrentHashMap<String, CompletableFuture<Void>> BARRIERS = new ConcurrentHashMap<>();

    @Inject
    Mutiny.Session session;

    @Inject
    Mutiny.StatelessSession statelessSession;

    @POST
    @Path("/book/{authorId}/{title}")
    @Transactional
    public Uni<Response> createBook(Integer authorId, String title) {
        Book book = new Book();
        book.setAuthor(authorId);
        book.setTitle(title);
        return session.persist(book)
                .replaceWith(() -> Response.status(Response.Status.CREATED).build());
    }

    @POST
    @Path("/book/fail/{authorId}/{title}")
    @Transactional
    public Uni<Response> createBookAndFail(Integer authorId, String title) {
        Book book = new Book();
        book.setAuthor(authorId);
        book.setTitle(title);
        return session.persist(book)
                .chain(() -> Uni.createFrom().failure(new RuntimeException("Forced failure for rollback test")));
    }

    @GET
    @Path("/thread")
    @Transactional
    public Uni<String> getThreadInfo() {
        return Uni.createFrom().item(Thread.currentThread().getName());
    }

    @GET
    @Path("/thread/blocking")
    @Transactional
    @Blocking
    public Uni<String> getThreadInfoBlocking() {
        return Uni.createFrom().item(Thread.currentThread().getName());
    }

    @GET
    @Path("/txtype/requires-new")
    @Transactional(TxType.REQUIRES_NEW)
    public Uni<String> requiresNew() {
        return Uni.createFrom().item("ok");
    }

    @GET
    @Path("/txtype/mandatory")
    @Transactional(TxType.MANDATORY)
    public Uni<String> mandatory() {
        return Uni.createFrom().item("ok");
    }

    @POST
    @Path("/stateless/{authorId}/{title}")
    @Transactional
    public Uni<Response> createBookStateless(Integer authorId, String title) {
        Book book = new Book();
        book.setAuthor(authorId);
        book.setTitle(title);
        return statelessSession.insert(book)
                .replaceWith(() -> Response.status(Response.Status.CREATED).build());
    }

    @GET
    @Path("/stateless/{title}")
    @Transactional
    public Uni<Response> getBookStateless(String title) {
        return findBookStatelessByTitle(title)
                .onItem().ifNotNull().transform(book -> Response.ok(book).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/stateless/{title}/{newTitle}")
    @Transactional
    public Uni<Response> updateBookStateless(String title, String newTitle) {
        return findBookStatelessByTitle(title)
                .map(book -> {
                    book.setTitle(newTitle);
                    return book;
                })
                .onItem().call(book -> statelessSession.update(book))
                .replaceWith(() -> Response.noContent().build());
    }

    @DELETE
    @Path("/stateless/{title}")
    @Transactional
    public Uni<Response> deleteBookStateless(String title) {
        return findBookStatelessByTitle(title)
                .call(book -> statelessSession.delete(book))
                .replaceWith(() -> Response.noContent().build());
    }

    private Uni<Book> findBookStatelessByTitle(String title) {
        return statelessSession.createQuery("from Book where title = :title", Book.class)
                .setParameter("title", title)
                .getSingleResultOrNull();
    }

    @POST
    @Path("/book/isolated/{authorId}/{title}")
    @Transactional
    public Uni<Response> createBookWithBarrier(Integer authorId, String title,
            @QueryParam("rollback") @DefaultValue("false") boolean rollback,
            @QueryParam("barrier") String barrier) {
        Book book = new Book();
        book.setAuthor(authorId);
        book.setTitle(title);
        return session.persist(book)
                .call(session::flush)
                .chain(() -> waitForBarrier(barrier))
                .chain(() -> rollback
                        ? Uni.createFrom().failure(new RuntimeException("Forced failure for rollback test"))
                        : Uni.createFrom().voidItem())
                .replaceWith(() -> Response.status(Response.Status.CREATED).build());
    }

    private Uni<Void> waitForBarrier(String barrierName) {
        return Uni.createFrom().deferred(() -> {
            Context ctx = Vertx.currentContext();
            CompletableFuture<Void> future = BARRIERS.compute(barrierName, (key, existing) -> {
                if (existing == null) {
                    return new CompletableFuture<>();
                } else {
                    existing.complete(null);
                    return existing;
                }
            });
            return Uni.createFrom().completionStage(future)
                    .emitOn(command -> ctx.runOnContext(v -> command.run()))
                    .ifNoItem().after(Duration.ofSeconds(10))
                    .failWith(new RuntimeException("Barrier timeout"));
        });
    }

    @GET
    @Path("/multi")
    @Transactional
    public Multi<Book> getMulti() {
        return Multi.createFrom().item(new Book());
    }
}
