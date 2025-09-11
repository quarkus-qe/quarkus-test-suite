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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.query.range.Range;

import io.quarkus.arc.InjectableInstance;
import io.quarkus.ts.jakarta.data.db.Author;
import io.quarkus.ts.jakarta.data.db.AuthorRepository;
import io.quarkus.ts.jakarta.data.db.Book;
import io.quarkus.ts.jakarta.data.db.BookRepository;
import io.quarkus.ts.jakarta.data.db.Db2AuthorRepository;
import io.quarkus.ts.jakarta.data.db.Db2BookRepository;
import io.quarkus.ts.jakarta.data.db.MariaDbAuthorRepository;
import io.quarkus.ts.jakarta.data.db.MariaDbBookRepository;
import io.quarkus.ts.jakarta.data.db.MySQLAuthorRepository;
import io.quarkus.ts.jakarta.data.db.MySQLBookRepository;
import io.quarkus.ts.jakarta.data.db.OracleAuthorRepository;
import io.quarkus.ts.jakarta.data.db.OracleBookRepository;
import io.quarkus.ts.jakarta.data.db.PgAuthorRepository;
import io.quarkus.ts.jakarta.data.db.PgBookRepository;
import io.quarkus.ts.jakarta.data.db.SqlServerAuthorRepository;
import io.quarkus.ts.jakarta.data.db.SqlServerBookRepository;

@Path("/basic-repository")
public final class BasicRepositoryResource {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public BasicRepositoryResource(InjectableInstance<AuthorRepository> authorRepositoryInstance,
            InjectableInstance<BookRepository> bookRepositoryInstance,
            @ConfigProperty(name = "quarkus.profile") String datasource) {
        authorRepository = switch (datasource) {
            case "pg" -> authorRepositoryInstance.select(PgAuthorRepository.class).get();
            case "mariadb" -> authorRepositoryInstance.select(MariaDbAuthorRepository.class).get();
            case "mysql" -> authorRepositoryInstance.select(MySQLAuthorRepository.class).get();
            case "oracle" -> authorRepositoryInstance.select(OracleAuthorRepository.class).get();
            case "sql-server" -> authorRepositoryInstance.select(SqlServerAuthorRepository.class).get();
            case "db2" -> authorRepositoryInstance.select(Db2AuthorRepository.class).get();
            default -> throw new IllegalArgumentException("Unknown datasource: " + datasource);
        };
        bookRepository = switch (datasource) {
            case "pg" -> bookRepositoryInstance.select(PgBookRepository.class).get();
            case "mariadb" -> bookRepositoryInstance.select(MariaDbBookRepository.class).get();
            case "mysql" -> bookRepositoryInstance.select(MySQLBookRepository.class).get();
            case "oracle" -> bookRepositoryInstance.select(OracleBookRepository.class).get();
            case "sql-server" -> bookRepositoryInstance.select(SqlServerBookRepository.class).get();
            case "db2" -> bookRepositoryInstance.select(Db2BookRepository.class).get();
            default -> throw new IllegalArgumentException("Unknown datasource: " + datasource);
        };
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
