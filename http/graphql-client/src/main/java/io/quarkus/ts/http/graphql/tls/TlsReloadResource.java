package io.quarkus.ts.http.graphql.tls;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.tls.CertificateUpdatedEvent;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;

@Path("/tls")
public class TlsReloadResource {
    @Inject
    TlsConfigurationRegistry registry;

    @Inject
    Event<CertificateUpdatedEvent> certificateUpdatedEvent;

    @POST
    @Path("/reload/{name}")
    public void reloadTlsConfiguration(@PathParam("name") String name) {
        TlsConfiguration config = registry.get(name).orElseThrow();

        if (config.reload()) {
            certificateUpdatedEvent.fire(new CertificateUpdatedEvent(name, config));
        } else {
            throw new IllegalStateException("Config " + name + " was not fired");
        }

    }
}
