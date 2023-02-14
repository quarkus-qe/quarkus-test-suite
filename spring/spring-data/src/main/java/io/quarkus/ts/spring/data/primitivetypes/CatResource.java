package io.quarkus.ts.spring.data.primitivetypes;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import io.quarkus.ts.spring.data.primitivetypes.model.Cat;

@Path("/cat")
public class CatResource {

    private final CatRepository catRepository;

    public CatResource(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    @GET
    @Path("/customFindDistinctivePrimitive/{id}")
    @Produces("text/plain")
    public Boolean customFindDistinctivePrimitive(@PathParam("id") Long id) {
        return catRepository.customFindDistinctivePrimitive(id);
    }

    @GET
    @Path("/customFindDistinctiveObject/{id}")
    @Produces("text/plain")
    public Boolean customFindDistinctiveObject(@PathParam("id") Long id) {
        return catRepository.customFindDistinctiveObject(id);
    }

    @GET
    @Path("/findCatsByMappedSuperclassField/{deathReason}")
    @Produces("application/json")
    public List<Cat> findCatsByMappedSuperclassField(@PathParam("deathReason") String deathReason) {
        return catRepository.findCatsByDeathReason(deathReason);
    }
}
