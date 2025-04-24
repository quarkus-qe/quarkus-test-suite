package io.quarkus.ts.http.restclient.reactive.resources;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.RestQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.ts.http.restclient.reactive.BookClient;
import io.quarkus.ts.http.restclient.reactive.json.Author;
import io.quarkus.ts.http.restclient.reactive.json.Book;
import io.quarkus.ts.http.restclient.reactive.json.BookIdWrapper;
import io.quarkus.ts.http.restclient.reactive.json.BookRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/books")
public class PlainBookResource {

    public static final String SEARCH_TERM_VAL = "Ernest Hemingway";
    private final URI baseUri;

    public PlainBookResource(@ConfigProperty(name = "quarkus.http.port") int httpPort) {
        this.baseUri = URI.create("http://localhost:" + httpPort);
    }

    @GET
    @Path("/rest-query")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Book>> getRestQuery(@RestQuery Integer firstPlainId, @RestQuery Integer secondPlainId,
            @RestQuery BookIdWrapper firstObjectId, @RestQuery BookIdWrapper secondObjectId,
            @RestQuery List<Integer> additionalIds) {
        var books = new ArrayList<Book>();
        books.add(BookRepository.getById(firstPlainId));
        books.add(BookRepository.getById(secondPlainId));
        books.add(BookRepository.getById(firstObjectId.getId()));
        books.add(BookRepository.getById(secondObjectId.getId()));
        additionalIds.stream().map(BookRepository::getById).forEach(books::add);
        return Uni.createFrom().item(books);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> getBook(@QueryParam("title") String title, @QueryParam("author") String author) {
        return Uni.createFrom().item(new Book(title, author));
    }

    @GET
    @Path("/author/name")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getAuthorName(@QueryParam("author") String author) {
        return Uni.createFrom().item(author);
    }

    @GET
    @Path("/author/info")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Author> getAuthor(@QueryParam("name") String author) {
        boolean hasNobel = author.contains("Hemingway");
        return Uni.createFrom().item(new Author(author, hasNobel));
    }

    @GET
    @Path("/author/profession/title")
    public Uni<String> getProfession() {
        return Uni.createFrom().item("writer");
    }

    @GET
    @Path("/author/profession/wage/currency/name")
    public Uni<String> getCurrency() {
        return Uni.createFrom().item("USD");
    }

    /**
     * Characters in foreign language: '%E3%82%AF%E3%82%A4%E3%83%83%E3%82%AF%E6%A4%9C%E7%B4%A2' -> 'クイック検索' -> 'quick-search'
     * Reserved characters: '%25%20%23%20%5B%20%5D%20+%20=%20&%20@%20:%20!%20*%20(%20)%20'%20$%20,%20%3F' -> "% # [ ] + = & @ :
     * ! * ( ) ' $ , ?",
     * characters '=', '+', '@', ':', '!', '*', '(', ')', '\'', ',' are not encoded as it's not necessary.
     * Unreserved characters: - _ . ~
     */
    @GET
    @Path("/%E3%82%AF%E3%82%A4%E3%83%83%E3%82%AF%E6%A4%9C%E7%B4%A2/%25%20%23%20%5B%20%5D%20+%20=%20&%20@%20:%20!%20*%20(%20)%20'%20$%20,%20%3F/-%20_%20.%20~")
    public Multi<String> getBySearchTerm(@QueryParam("searchTerm") String searchTerm) {
        if (SEARCH_TERM_VAL.equals(searchTerm)) {
            return Multi.createFrom().items("In Ou"
                    + "r Time", ", ", "The Sun Also Rises", ", ", "A Farewell to Arms", ", ",
                    "The Old Man and the Sea");
        } else {
            return Multi.createFrom().empty();
        }
    }

    @POST
    @Path("/author/books")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Multi<String> booksByAuthor(Author author) {
        if (SEARCH_TERM_VAL.equals(author.name()) && author.gotNobelPrize()) {
            return Multi.createFrom().items("In Ou"
                    + "r Time", ", ", "The Sun Also Rises", ", ", "A Farewell to Arms", ", ",
                    "The Old Man and the Sea");
        } else {
            return Multi.createFrom().empty();
        }
    }

    @GET
    @Path("/suffix")
    @Produces("application/text+json")
    public Uni<String> getWithSuffixedType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_text+json");
    }

    @GET
    @Path("/suffix")
    @Produces("application/text")
    public Uni<String> getWithSubType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_text");
    }

    @GET
    @Path("/suffix")
    @Produces("application/json")
    public Uni<String> getWithSuffix(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_json");
    }

    @GET
    @Path("/suffix")
    @Produces("application/quarkus")
    public Uni<String> getWithUnrelatedType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_other");
    }

    @GET
    @Path("/suffix_priority")
    @Produces("application/text")
    public Uni<String> getPriorityWithSubType(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_text");
    }

    @GET
    @Path("/suffix_priority")
    @Produces("application/json")
    public Uni<String> getPriorityWithSuffix(@QueryParam("content") String text) {
        return Uni.createFrom().item(text + "_json");
    }

    @GET
    @Path("/programmatic-way")
    public Uni<String> programmaticRestClient()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        return RestClientBuilder
                .newBuilder()
                .baseUri(baseUri)
                .trustStore(trustStore()) // keep truststore in place to verify QUARKUS-3170
                .build(BookClient.class)
                .getBook("The Hobbit: An Unexpected Journey", "J. R. R. Tolkien")
                .map(Book::title);
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Book> searchBooks(@FormParam("author") String author) {
        return Set.of(new Book("The Wind-Up Bird Chronicle", author));
    }

    @POST
    @Path("/sequel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getSequel(Book first) {
        return Uni.createFrom().item(first.title() + " II");
    }

    @GET
    @Path("/direct-client")
    @Produces(MediaType.APPLICATION_JSON)
    public Book client(@QueryParam("title") String title, @QueryParam("author") String author) {
        WebTarget target;
        try (Client client = ClientBuilder.newClient()) {
            target = client.target(baseUri.resolve("/books"))
                    .queryParam("title", title)
                    .queryParam("author", author);
            target.register(new JacksonObjectMapperContextResolver());
            jakarta.ws.rs.core.Response response = target.request(MediaType.APPLICATION_JSON).get();
            return response.readEntity(Book.class);
        }
    }

    private KeyStore trustStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (var is = getClass().getResourceAsStream("META-INF/keystore.jks")) {
            ks.load(is, "password".toCharArray());
        }
        return ks;
    }

    static class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return CDI.current().select(ObjectMapper.class).get();
        }
    }
}
