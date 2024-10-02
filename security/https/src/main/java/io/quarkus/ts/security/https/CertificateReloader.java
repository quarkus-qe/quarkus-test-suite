package io.quarkus.ts.security.https;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;

import io.quarkus.tls.CertificateUpdatedEvent;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class CertificateReloader {

    private static final String MTLS_CONFIG_NAME = "mtls-http";

    void setupCertificateReloadingTriggerRoute(@Observes Router router, Event<CertificateUpdatedEvent> certUpdatedEvent,
            TlsConfigurationRegistry registry, Vertx vertx) {
        router.route("/reload-mtls-certificates").handler(ctx -> {
            TlsConfiguration config = registry.get(MTLS_CONFIG_NAME).orElseThrow();
            vertx
                    .executeBlocking(() -> {
                        boolean reloaded = config.reload();
                        if (reloaded) {
                            certUpdatedEvent.fire(new CertificateUpdatedEvent(MTLS_CONFIG_NAME, config));
                        }
                        return reloaded;
                    })
                    .onSuccess(reloaded -> {
                        if (reloaded) {
                            ctx.response().end("Certificates reloaded.");
                        } else {
                            ctx.fail(500, new RuntimeException("Could not reload mTLS certificates."));
                        }
                    })
                    .onFailure(ctx::fail);
        });
    }

}
