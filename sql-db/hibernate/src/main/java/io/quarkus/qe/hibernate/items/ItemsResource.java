package io.quarkus.qe.hibernate.items;

import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.hibernate.jpa.SpecHints;

import io.quarkus.hibernate.orm.PersistenceUnit;

@Path("/items")
@Transactional
public class ItemsResource {

    @PersistenceUnit("named")
    @Inject
    EntityManager em;

    @GET
    @Path("/count")
    public int countOrders() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> cq = cb.createQuery(Item.class);
        // Eager fetch the relationship between item and customer
        // Do not remove as this is the actual reproducer for https://github.com/quarkusio/quarkus/issues/14201 and
        // https://github.com/quarkusio/quarkus/issues/14881.
        cq.from(Item.class).fetch("customer");

        TypedQuery<Item> query = em.createQuery(cq);
        return query.getResultList().size();
    }

    @Transactional
    @GET
    @Path("/{id}")
    public String getCustomer(@PathParam("id") Long id) {
        return em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class)
                .setParameter("id", id)
                .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, em.getEntityGraph("Item.withCustomer"))
                .getResultList()
                .stream()
                .map(i -> i.customer.id)
                .sorted(Long::compareTo)
                .map(Object::toString)
                .collect(Collectors.joining("-"));
    }

}
