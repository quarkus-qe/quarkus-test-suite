package io.quarkus.ts.security.jwt;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.PermissionChecker;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;

@GraphQLApi
public class GraphQLVirtualThreadResource {

    @Inject
    JsonWebToken jwt;

    @PermissionsAllowed("forbid_austria")
    @RunOnVirtualThread
    @Query("subject_vt")
    public String getSubject() {
        failIfNotVirtualThread();
        return jwt.getSubject();
    }

    @PermissionChecker("forbid_austria")
    boolean forbidAustria(SecurityIdentity securityIdentity) {
        DefaultJWTCallerPrincipal principal = (DefaultJWTCallerPrincipal) securityIdentity.getPrincipal();
        return !"Austria".equals(principal.getSubject());
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
