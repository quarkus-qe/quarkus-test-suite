package io.quarkus.ts.jakarta.data.rest;

import java.util.List;

import jakarta.data.Order;
import jakarta.data.page.PageRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import io.quarkus.ts.jakarta.data.db.DayOfWeek;
import io.quarkus.ts.jakarta.data.db.Fruit;
import io.quarkus.ts.jakarta.data.db.FruitCrudRepository;

@Path("/crud-repository")
public final class CrudRepositoryResource {

    private final FruitCrudRepository fruitCrudRepository;

    CrudRepositoryResource(FruitCrudRepository fruitCrudRepository) {
        this.fruitCrudRepository = fruitCrudRepository;
    }

    @Transactional
    @Path("/builtin/insert")
    @POST
    public Fruit insertFruit(Fruit fruit) {
        return fruitCrudRepository.insert(fruit);
    }

    @Path("/builtin/find-by-id/{fruit-id}")
    @GET
    public Fruit getFruitById(@PathParam("fruit-id") long fruitId) {
        return fruitCrudRepository
                .findById(fruitId)
                .orElseThrow(() -> new IllegalArgumentException("There is no Fruit with id " + fruitId));
    }

    @Transactional
    @Path("/builtin/update")
    @PUT
    public Fruit updateFruit(Fruit fruit) {
        return fruitCrudRepository.update(fruit);
    }

    @Transactional
    @Path("/builtin/insert-all")
    @POST
    public List<Fruit> insertFruit(List<Fruit> fruits) {
        return fruitCrudRepository.insertAll(fruits);
    }

    @Path("/builtin/find-all")
    @GET
    public List<Fruit> findAllFruits() {
        return fruitCrudRepository.findAll().toList();
    }

    @Transactional
    @Path("/builtin/delete-by-id/{fruit-id}")
    @DELETE
    public void deleteFruitById(@PathParam("fruit-id") long fruitId) {
        fruitCrudRepository.deleteById(fruitId);
    }

    @Path("/query-annotation/jdql/count")
    @GET
    public long count() {
        return fruitCrudRepository.countFruits();
    }

    @Path("/builtin/find-all-with-page-and-order")
    @GET
    public List<Fruit> findAllFruitsWithPageAndOrder(@QueryParam("pageNumber") int pageNumber,
            @QueryParam("pageSize") int pageSize) {
        var pageRequest = PageRequest.ofPage(pageNumber, pageSize, true);
        var order = Order.<Fruit> by(io.quarkus.ts.jakarta.data.db._Fruit.id.asc());
        return fruitCrudRepository.findAll(pageRequest, order).content();
    }

    @Transactional
    @Path("/builtin/delete")
    @DELETE
    public void deleteFruitById(Fruit fruit) {
        fruitCrudRepository.delete(fruit);
    }

    @POST
    @Path("/exists-by-day-of-week")
    public boolean existsByDayOWeek(DayOfWeek dayOfWeek) {
        return fruitCrudRepository.existsByDayOfWeek(dayOfWeek);
    }
}
