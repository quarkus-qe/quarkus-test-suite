package io.quarkus.ts.hibernate.startup.offline.test.reactive;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import org.hibernate.reactive.mutiny.Mutiny;

import io.smallrye.mutiny.Uni;

@Path("reactive")
public class ReactiveResource {

    private final Mutiny.SessionFactory sessionFactory;

    ReactiveResource(Mutiny.SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Path("create-table")
    @POST
    public Uni<Void> createTable() {
        return sessionFactory.withSession(session -> session
                .createNativeQuery("CREATE TABLE article(id BIGSERIAL PRIMARY KEY, name VARCHAR(250))")
                .executeUpdate())
                .replaceWithVoid();
    }

    @Path("/article")
    @POST
    public Uni<Void> createArticle(ReactiveArticle article) {
        return sessionFactory.withTransaction(session -> session.persist(article));
    }

    @Produces(APPLICATION_JSON)
    @Path("/article/{id}")
    @GET
    public Uni<ReactiveArticle> getArticleById(@PathParam("id") Long id) {
        return sessionFactory.withStatelessSession(statelessSession -> statelessSession.get(ReactiveArticle.class, id));
    }

}
