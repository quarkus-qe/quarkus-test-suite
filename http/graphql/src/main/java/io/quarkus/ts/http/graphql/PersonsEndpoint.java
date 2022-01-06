package io.quarkus.ts.http.graphql;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class PersonsEndpoint {
    private final Person[] philosophers;

    public PersonsEndpoint() {
        final Person plato = new Person("Plato");
        final Person aristotle = new Person("Aristotle");
        plato.setFriend(aristotle);
        aristotle.setFriend(plato);
        philosophers = new Person[] { plato, aristotle };
    }

    @Query("philosophers")
    @Description("Get a couple of Greek philosophers")
    public Person[] getPhilosophers() {
        return philosophers;
    }
}
