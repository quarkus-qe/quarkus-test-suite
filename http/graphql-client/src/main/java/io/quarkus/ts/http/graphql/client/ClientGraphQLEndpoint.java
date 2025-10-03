package io.quarkus.ts.http.graphql.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.common.annotation.RunOnVirtualThread;

@GraphQLApi
public class ClientGraphQLEndpoint {

    @Inject
    GraphQLClient client;

    @RunOnVirtualThread
    @Query("date_vt")
    @Description("Queries the date endpoint")
    public String getPhilosophers() {
        failIfNotVirtualThread();
        OffsetDateTime dateOfWriting = OffsetDateTime.of(
                LocalDate.of(2025, Month.MARCH, 13),
                LocalTime.of(11, 47, 13),
                ZoneOffset.ofHours(1));
        return client.processDate(dateOfWriting);
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
