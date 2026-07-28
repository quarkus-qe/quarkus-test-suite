package io.quarkus.ts.security.keycloak.webapp;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.AuthenticationCompletionAction;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Unremovable
public class AuthenticationCompletionSecondary implements AuthenticationCompletionAction {

    private final AtomicInteger callCount = new AtomicInteger();

    @Override
    public Uni<Void> action(AuthenticationCompletionContext authCompletionContext) {
        callCount.incrementAndGet();
        return Uni.createFrom().voidItem();
    }

    public int getCallCount() {
        return callCount.get();
    }

    public void reset() {
        callCount.set(0);
    }
}
