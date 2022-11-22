package io.quarkus.ts.http.graphql;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class PersonsEndpoint extends PersonsEndpointBase {

    @Query("philosophers")
    @Description("Get a couple of Greek philosophers")
    public List<Person> getPhilosophers() {
        return philosophers;
    }

    @Query("friend")
    public Person getPhilosopher(@Name("name") String name) {
        for (Person philosopher : philosophers) {
            if (philosopher.getName().equals(name)) {
                return philosopher.getFriend();
            }
        }
        throw new NoSuchElementException(name);
    }

    @Mutation("create")
    public Person createPhilosopher(@Name("name") String name) {
        Person philosopher = new Person(name);
        philosophers.add(philosopher);
        return philosopher;
    }

    @Query("map")
    public Map<PhilosophyEra, Person> getPhilosophersMap() {
        return philosophersMap;
    }

    @Query("error")
    public String throwError() throws PhilosophyException {
        throw new PhilosophyException();
    }
}
