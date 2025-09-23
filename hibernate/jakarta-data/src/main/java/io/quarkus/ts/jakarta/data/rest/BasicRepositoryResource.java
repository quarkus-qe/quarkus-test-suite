package io.quarkus.ts.jakarta.data.rest;

import java.util.List;

import jakarta.data.Limit;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.hibernate.query.range.Range;

import io.quarkus.ts.jakarta.data.db.Author;
import io.quarkus.ts.jakarta.data.db.AuthorRepository;
import io.quarkus.ts.jakarta.data.db.Book;
import io.quarkus.ts.jakarta.data.db.BookRepository;

@Path("/basic-repository")
public final class BasicRepositoryResource {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    BasicRepositoryResource(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    @Path("/save-all")
    @POST
    public List<Author> saveAll(List<Author> authors) {
        bookRepository.saveAll(authors.stream().flatMap(a -> a.getBooks().stream()).toList());
        // TODO: switch to the 'authorRepository.saveAll' once https://github.com/quarkusio/quarkus/issues/49593 is fixed
        bookRepository.session().insertMultiple(authors);
        return authors;
    }

    @Path("/find-annotation-with-limit-and-order-by")
    @GET
    public List<Book> findBooksByTitle(@QueryParam("limit") int limit, @QueryParam("name-prefix") String namePrefix) {
        Range<String> titleRange = Range.prefix(namePrefix);
        Limit maxResults = Limit.of(limit);
        return bookRepository.findByTitle(titleRange, maxResults);
    }

    @Transactional
    @Path("/author/{author-id}/delete-using-stateless-session-directly")
    @DELETE
    public void deleteBookWithStatelessSession(Book book, @PathParam("author-id") Long authorId) {
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author with id " + authorId + " does not exist"));
        var session = bookRepository.session();
        session.fetch(author.getBooks());
        author.getBooks().remove(book);
        // TODO: replace the next line with session.update(author)
        //   after https://github.com/quarkusio/quarkus/issues/49593 is fixed
        authorRepository.save(author);
        session.delete(book);
    }

    @GET
    @Path("/author/{author-id}/builtin/find-by-id")
    public Author findAuthorById(@PathParam("author-id") long authorId) {
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author with id " + authorId + " does not exist"));
        bookRepository.session().fetch(author.getBooks());
        return author;
    }

}
