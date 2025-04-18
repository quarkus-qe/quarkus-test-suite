package io.quarkus.ts.websockets.next;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.websockets.next.HttpUpgradeCheck;
import io.smallrye.mutiny.Uni;
import io.vertx.core.MultiMap;

@ApplicationScoped
public class QuarkusQEUpgradeCheck implements HttpUpgradeCheck {
    @Override
    public Uni<CheckResult> perform(HttpUpgradeContext ctx) {
        if (rejectUpgrade(ctx)) {
            return CheckResult.rejectUpgrade(400);
        }
        return CheckResult.permitUpgrade();
    }

    private boolean rejectUpgrade(HttpUpgradeContext ctx) {
        MultiMap headers = ctx.httpRequest().headers();
        return headers.contains("Reject");
    }
}
