package io.quarkus.ts.http.graphql;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.common.annotation.RunOnVirtualThread;

@GraphQLApi
public class PersonsVirtualThreadEndpoint extends PersonsEndpointBase {

    @RunOnVirtualThread
    @Query("philosophers_vt")
    @Description("Get a couple of Greek philosophers")
    public List<Person> getPhilosophers() {
        failIfNotVirtualThread();
        return philosophers;
    }

    @RunOnVirtualThread
    @Query("friend_vt")
    public Person getPhilosopher(@Name("name") String name) {
        failIfNotVirtualThread();
        for (Person philosopher : philosophers) {
            if (philosopher.getName().equals(name)) {
                return philosopher.getFriend();
            }
        }
        throw new NoSuchElementException(name);
    }

    @RunOnVirtualThread
    @Mutation("create_vt")
    public Person createPhilosopher(@Name("name") String name) {
        failIfNotVirtualThread();
        Person philosopher = new Person(name);
        philosophers.add(philosopher);
        return philosopher;
    }

    @RunOnVirtualThread
    @Query("map_vt")
    public Map<PhilosophyEra, Person> getPhilosophersMap() {
        failIfNotVirtualThread();
        return philosophersMap;
    }

    @RunOnVirtualThread
    @Query("error_vt")
    public String throwError() throws PhilosophyException {
        failIfNotVirtualThread();
        throw new PhilosophyException();
    }

    private static void failIfNotVirtualThread() {
        if (!isVirtualThread()) {
            throw new IllegalStateException("This method must be executed on a virtual thread: " + Thread.currentThread());
        }
    }

    private static boolean isVirtualThread() {
        try {
            return (boolean) findVirtualMethodHandle().invokeExact(Thread.currentThread());
        } catch (Throwable t) {
            return false;
        }
    }

    // this code comes from Quarkus core
    private static MethodHandle findVirtualMethodHandle() {
        try {
            return MethodHandles.publicLookup().findVirtual(Thread.class, "isVirtual",
                    MethodType.methodType(boolean.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
