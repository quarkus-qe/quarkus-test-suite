package io.quarkus.ts.hibernate.startup.offline.pu.defaults.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.ts.hibernate.startup.offline.pu.defaults.orm.Article;

@Path("/default-pu/newspapers/{tenant}/")
public class NewspapersResource {

    @Inject
    EntityManager entityManager;

    @Inject
    @PersistenceUnit
    SessionFactory sessionFactory;

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

    @Path("/dialect/max-varchar-length")
    @GET
    public int getMaxVarcharLength() {
        var dialect = (MariaDBDialect) sessionFactory.unwrap(SessionFactoryImplementor.class).getJdbcServices().getDialect();
        return dialect.getMaxVarcharLength();
    }
}
