package io.quarkus.ts.http.graphql;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Context;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@GraphQLApi
public class PersonsReactiveEndpoint extends PersonsEndpointBase {

    @Query("friend_reactive")
    public Uni<Person> getPhilosopherReactively(@Name("name") String name) {
        return getPhilosopherUni(name);
    }

    @Query("friend_reactive_default")
    public Uni<Person> getPhilosopherWithDefaultName(@Name("name") @DefaultValue("Plato") String name) {
        return getPhilosopherUni(name);
    }

    private Uni<Person> getPhilosopherUni(String name) {
        return Multi.createFrom().iterable(philosophers)
                .filter(person -> person.getName().equals(name))
                .map(Person::getFriend)
                .toUni();
    }

    @Query("echo_context_path_reactive")
    public Uni<String> echoContextPath(Context context) {
        return Uni.createFrom().item(context.getPath());
    }

    @Mutation("create_reactive")
    public Uni<Person> createPhilosopherReactively(@Name("name") String name) {
        Person philosopher = new Person(name);
        philosophers.add(philosopher);
        return Uni.createFrom().item(philosopher);
    }
}
