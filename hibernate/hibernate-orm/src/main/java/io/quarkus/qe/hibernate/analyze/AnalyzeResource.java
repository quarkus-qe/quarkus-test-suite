package io.quarkus.qe.hibernate.analyze;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("/analyze")
public class AnalyzeResource {

    public static final String AUTHOR = "Churchill";

    @PersistenceUnit("named")
    @Inject
    EntityManager entityManager;

    @Path("/{id}/author")
    @GET
    public String getAnalyzeAuthor(@PathParam("id") long id) {
        return entityManager.find(Analyze.class, id).author;
    }

    @Transactional
    @POST
    public long create() {
        var analyze = new Analyze(1L, AUTHOR);
        entityManager.persist(analyze);
        return analyze.id;
    }

}
