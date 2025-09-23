package io.quarkus.ts.jakarta.data.rest;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkus.ts.jakarta.data.db.Fruit;
import io.quarkus.ts.jakarta.data.db.FruitRepository;
import io.quarkus.ts.jakarta.data.db.OtherFruitRepository;

@Path("/repository")
public final class RepositoryResource {

    private final FruitRepository fruitRepository;
    private final OtherFruitRepository otherFruitRepository;

    RepositoryResource(FruitRepository fruitRepository, OtherFruitRepository otherFruitRepository) {
        this.fruitRepository = fruitRepository;
        this.otherFruitRepository = otherFruitRepository;
    }

    @Path("/query-with-record")
    @GET
    public FruitRepository.View getCurrentView() {
        return fruitRepository.getCurrentView();
    }

    @PUT
    @Path("/update")
    public void update(Fruit fruit) {
        otherFruitRepository.update(fruit);
    }

    @GET
    @Path("/find-by-name")
    public Fruit findByName(@QueryParam("name") String name) {
        return otherFruitRepository.findByName(name);
    }

    @Transactional
    @POST
    @Path("/insert")
    public void insert(Fruit fruit) {
        otherFruitRepository.insert(fruit);
    }

    @GET
    @Path("/find-by-pattern")
    public Fruit findByPattern(@QueryParam("name") String name) {
        String pattern = '%' + name + '%';
        return otherFruitRepository.findByNamePattern(pattern);
    }

    @Transactional
    @DELETE
    @Path("/delete-by-name")
    public int deleteByName(@QueryParam("name") String name) {
        return otherFruitRepository.deleteByName(name);
    }

    @GET
    @Path("/find-all")
    public List<Fruit> findAll() {
        return otherFruitRepository.findAll();
    }

    @Transactional
    @PATCH
    @Path("/save")
    public void save(Fruit fruit) {
        otherFruitRepository.save(fruit);
    }

    @GET
    @Path("/find-by-id")
    public Fruit findByPattern(@QueryParam("id") Long id) {
        return otherFruitRepository.findById(id);
    }

    @GET
    @Path("/query-with-jdql-function-call")
    public List<FruitRepository.NameLengths> getNameLengths() {
        return fruitRepository.getNameLengths();
    }

    @GET
    @Path("/query-with-enum")
    public List<Fruit> getFruitsNotFromMonday() {
        return fruitRepository.getFruitsNotFromMonday();
    }

    @GET
    @Path("/query-with-cdi-security-interceptor")
    public List<Long> queryFruitsIfYouAreAuthenticated() {
        return fruitRepository.getAllFruitIdsSecurityInterceptor();
    }

    @GET
    @Path("/query-with-cdi-custom-interceptor")
    public List<Long> neverGetFruitsBecauseCustomCdiInterceptorApplied() {
        return fruitRepository.getAllFruitIdsCustomInterceptor();
    }

    @Transactional
    @DELETE
    @Path("/clean-up-fruit-table")
    public void cleanUpFruitTable() {
        fruitRepository.cleanUpFruitTable();
    }

    @GET
    @Path("/dev-mode/query-with-wrong-enum-constant")
    public List<Fruit> useQueryWithWrongEnumConstant() {
        return fruitRepository.getFruitsNotFromThursday();
    }
}
