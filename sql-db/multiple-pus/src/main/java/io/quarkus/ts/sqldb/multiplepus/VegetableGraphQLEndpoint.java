package io.quarkus.ts.sqldb.multiplepus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.ts.sqldb.multiplepus.model.vegetable.Vegetable;
import io.smallrye.common.annotation.RunOnVirtualThread;

@GraphQLApi
public class VegetableGraphQLEndpoint {

    @Transactional
    @RunOnVirtualThread
    @Mutation("vegetable")
    public Vegetable create(@Name("name") String name) {
        failIfNotVirtualThread();
        Vegetable vegetable = new Vegetable();
        vegetable.name = name;
        vegetable.persist();
        return vegetable;
    }

    @RunOnVirtualThread
    @Query("vegetable")
    public Vegetable get(@Name("id") Long id) {
        failIfNotVirtualThread();
        final Vegetable vegetable = Vegetable.findById(id);
        if (vegetable == null) {
            throw new NotFoundException("vegetable '" + id + "' not found");
        }
        return vegetable;
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
