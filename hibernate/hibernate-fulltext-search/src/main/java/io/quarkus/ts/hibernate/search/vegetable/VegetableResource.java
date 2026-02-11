package io.quarkus.ts.hibernate.search.vegetable;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.hibernate.search.mapper.orm.session.SearchSession;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Path("/{tenant}/vegetables")
public class VegetableResource {

    public record NameAndDescription(String name, String description) {
    }

    @Inject
    SearchSession searchSession;

    @POST
    @Transactional
    public Response create(NameAndDescription nameAndDescription) {
        var vegetable = new Vegetable(nameAndDescription.name, nameAndDescription.description);
        vegetable.persist();
        return Response.ok(vegetable).status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/search")
    @Transactional
    @Blocking
    public Response search(@QueryParam("terms") String terms, @QueryParam("fieldName") String fieldName) {
        List<Vegetable> list = searchSession.search(Vegetable.class)
                .where(f -> f.simpleQueryString().field(fieldName).matching(terms))
                .fetchAllHits();
        return Response.status(Response.Status.OK).entity(list).build();
    }

}
