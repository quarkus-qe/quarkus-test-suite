package io.quarkus.ts.http.graphql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PersonsEndpointBase {
    protected final List<Person> philosophers = new ArrayList<>();

    protected final Map<PhilosophyEra, Person> philosophersMap = new HashMap<>();

    public PersonsEndpointBase() {
        final Person plato = new Person("Plato");
        final Person aristotle = new Person("Aristotle");
        final Person anaxagoras = new Person("Anaxagoras");
        plato.setFriend(aristotle);
        plato.setIdol(anaxagoras);
        aristotle.setFriend(plato);
        aristotle.setIdol(anaxagoras);
        philosophers.addAll(Arrays.asList(plato, aristotle, anaxagoras));
        philosophersMap.put(PhilosophyEra.PRE_SOCRATIC, anaxagoras);
        philosophersMap.put(PhilosophyEra.POST_SOCRATIC, plato);
    }
}
