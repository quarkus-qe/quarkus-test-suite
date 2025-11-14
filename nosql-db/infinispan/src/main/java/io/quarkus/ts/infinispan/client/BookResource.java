package io.quarkus.ts.infinispan.client;

import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.infinispan.client.hotrod.RemoteCache;

import io.quarkus.infinispan.client.Remote;

@Path("/books")
public class BookResource {

    @Inject
    @Remote("books")
    RemoteCache<String, Book> booksCache;

    @POST
    @Path("/commit")
    @Produces(MediaType.TEXT_PLAIN)
    public String addBooksWithCommit() {
        TransactionManager tm = booksCache.getTransactionManager();
        if (tm == null) {
            throw new WebApplicationException(
                    "TransactionManager is null - check cache transaction-mode configuration",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        try {
            tm.begin();
            booksCache.put("hp-1", new Book("Harry Potter 1"));
            booksCache.put("hp-2", new Book("Harry Potter 2"));
            booksCache.put("hp-3", new Book("Harry Potter 3"));
            tm.commit();
            return "Committed: 3 books added";
        } catch (Exception e) {
            try {
                tm.rollback();
            } catch (Exception rollbackEx) {
                e.addSuppressed(rollbackEx);
            }
            throw new WebApplicationException(
                    "Failed to commit transaction: " + e.getMessage(),
                    e,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/rollback")
    @Produces(MediaType.TEXT_PLAIN)
    public String addBooksWithRollback() {
        TransactionManager tm = booksCache.getTransactionManager();
        if (tm == null) {
            throw new WebApplicationException(
                    "TransactionManager is null - check cache transaction-mode configuration",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        try {
            tm.begin();
            booksCache.put("got-1", new Book("Game of Thrones 1"));
            booksCache.put("got-2", new Book("Game of Thrones 2"));
            tm.rollback();
            return "Rolled back: books NOT added";
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to rollback transaction: " + e.getMessage(),
                    e,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public int getCacheSize() {
        return booksCache.size();
    }

    @DELETE
    @Path("/clear")
    public void clearCache() {
        booksCache.clear();
    }
}