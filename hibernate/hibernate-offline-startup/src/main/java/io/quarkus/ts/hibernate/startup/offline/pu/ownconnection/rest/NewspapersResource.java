package io.quarkus.ts.hibernate.startup.offline.pu.ownconnection.rest;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.ts.hibernate.startup.offline.pu.ownconnection.orm.Article;

@Path("/own-connection-provider-pu/newspapers/{tenant}/")
public class NewspapersResource {

    @Named("own_connection_provider")
    @Inject
    EntityManager entityManager;

    @GET
    @Path("/article/count")
    public long count() {
        return (long) entityManager
                .createQuery("select count(*) from Article")
                .getSingleResultOrNull();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/article/{id}")
    public Article getArticle(@RestPath Long id) {
        return entityManager.find(Article.class, id);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @POST
    @Path("/article")
    public Long createArticle(Article article) {
        entityManager.persist(article);
        return article.getId();
    }

}
