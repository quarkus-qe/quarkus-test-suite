package io.quarkus.ts.hibernate.search.fruit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.predicate.dsl.TypedSearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.util.common.data.Range;
import org.jboss.logging.Logger;

import io.quarkus.ts.hibernate.search.aggregation.MetricAggregationsResponseDto;
import io.quarkus.ts.hibernate.search.aggregation.PriceAggregation;
import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Path("/{tenant}/fruits")
public class FruitResource {

    private static final Logger LOG = Logger.getLogger(FruitResource.class.getName());

    @Inject
    EntityManager entityManager;
    @Inject
    SearchSession searchSession;

    @GET
    @Path("/")
    @Transactional
    @Blocking
    public Fruit[] getAll() {
        return entityManager.createNamedQuery("Fruits.findAll", Fruit.class)
                .getResultList().toArray(new Fruit[0]);
    }

    @GET
    @Path("/{id}")
    @Transactional
    @Blocking
    public Fruit findById(int id) {
        Fruit entity = entityManager.find(Fruit.class, id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Path("/")
    @Transactional
    @Blocking
    public Response create(Fruit fruit) {
        if (fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        if (fruit.getProducers() != null && !fruit.getProducers().isEmpty()) {
            fruit.getProducers().forEach(producer -> producer.setFruit(fruit));
        }
        LOG.debugv("Create {0}", fruit.getName());
        entityManager.persist(fruit);
        return Response.ok(fruit).status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Blocking
    public Fruit update(@PathParam("id") int id, Fruit fruit) {
        if (fruit.getName() == null) {
            throw new WebApplicationException("Fruit Name was not set on request.", 422);
        }

        Fruit entity = entityManager.find(Fruit.class, id);
        if (entity == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.setName(fruit.getName());

        LOG.debugv("Update #{0} {1}", fruit.getId(), fruit.getName());

        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Blocking
    public Response delete(@PathParam("id") int id) {
        Fruit fruit = entityManager.getReference(Fruit.class, id);
        if (fruit == null) {
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        LOG.debugv("Delete #{0} {1}", fruit.getId(), fruit.getName());
        entityManager.remove(fruit);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/search")
    @Transactional
    @Blocking
    public Response search(@QueryParam("terms") String terms) {
        List<Fruit> list = searchSession.search(Fruit.class)
                .where(f -> f.simpleQueryString().field("name").matching(terms))
                .sort(f -> f.field("fruitName_sort"))
                .fetchAllHits();
        return Response.status(Response.Status.OK).entity(list).build();
    }

    @GET
    @Path("/search-using-metamodel-class")
    @Transactional
    @Blocking
    public Response searchUsingMetamodelClasses(@QueryParam("terms") String terms) {
        List<Fruit> list = searchSession.search(Fruit__.INDEX.scope(searchSession))
                .where(f -> f.simpleQueryString().field(Fruit__.INDEX.name).matching(terms))
                .sort(f -> f.field(Fruit__.INDEX.fruitName_sort).asc())
                .fetchAllHits();
        return Response.status(Response.Status.OK).entity(list).build();
    }

    @GET
    @Path("/metric-aggregations")
    public MetricAggregationsResponseDto getMetricAggregations(@QueryParam("fetch") int fetch) {
        AggregationKey<Double> avgPriceKey = AggregationKey.of("avgPrice");
        var result = searchSession.search(Fruit__.INDEX.scope(searchSession))
                .where(TypedSearchPredicateFactory::matchAll)
                .sort(f -> f.field(Fruit__.INDEX.fruitName_sort).asc())
                .aggregation(avgPriceKey, f -> f.avg().field(Fruit__.INDEX.price))
                .fetch(fetch);
        Double averagePrice = result.aggregation(avgPriceKey);
        return new MetricAggregationsResponseDto(averagePrice, result.hits().size());
    }

    @GET
    @Path("/range-aggregations")
    public FruitPriceReport getRangeAggregations() {
        AggregationKey<Map<Range<Double>, PriceAggregation>> priceAggregationsKey = AggregationKey
                .of("priceAggregationsKey");
        SearchResult<Fruit> result = searchSession.search(Fruit.class)
                .where(TypedSearchPredicateFactory::matchAll)
                .aggregation(priceAggregationsKey, f -> f.range()
                        .field("price", Double.class)
                        .range(0.0, 10.0)
                        .range(10.0, 20.0)
                        .range(20.0, null)
                        .value(f.composite()
                                .from(
                                        f.avg().field("price", Double.class),
                                        f.min().field("price", Double.class),
                                        f.max().field("price", Double.class))
                                .as(PriceAggregation::new)))
                .fetch(20);
        var aggregations = result.aggregation(priceAggregationsKey);
        AtomicReference<PriceAggregation> zeroToTen = new AtomicReference<>();
        AtomicReference<PriceAggregation> tenToTwenty = new AtomicReference<>();
        AtomicReference<PriceAggregation> twentyToInfinity = new AtomicReference<>();
        aggregations.forEach((range, priceAggregation) -> {
            double lowerBound = range.lowerBoundValue().orElseThrow();
            if (lowerBound == 0.0) {
                zeroToTen.set(priceAggregation);
            } else if (lowerBound == 10.0) {
                tenToTwenty.set(priceAggregation);
            } else if (lowerBound == 20.0) {
                twentyToInfinity.set(priceAggregation);
            }
        });
        return new FruitPriceReport(zeroToTen.get(), tenToTwenty.get(), twentyToInfinity.get());
    }

    @Transactional
    @DELETE
    @Path("/delete-by-price-range")
    public int deleteByPriceRange(@QueryParam("min-price") double minPrice, @QueryParam("max-price") double maxPrice) {
        List<Fruit> list = searchSession.search(Fruit__.INDEX.scope(searchSession))
                .where(f -> f.range().field(Fruit__.INDEX.price).between(minPrice, maxPrice))
                .fetchAllHits();
        list.forEach(entityManager::remove);
        return list.size();
    }

    @Path("/projection-with-multivalued-field")
    @GET
    public FruitProjection getProjectionWithMultivaluedField(@QueryParam("fruit-name") String fruitName) {
        return searchSession.search(Fruit__.INDEX.scope(searchSession))
                .select(FruitProjection.class)
                .where(f -> f.match().field(Fruit__.INDEX.name).matching(fruitName))
                .fetch(1)
                .hits().get(0);
    }

}
