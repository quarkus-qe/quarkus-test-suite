package io.quarkus.ts.http.restclient.reactive.resources;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.tls.CertificateUpdatedEvent;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;

@Path("/tls")
public class TLSResource {

    @Inject
    Event<CertificateUpdatedEvent> event;
    @Inject
    TlsConfigurationRegistry registry;

    @POST
    @Path("/reload/{name}")
    public void reload(@PathParam("name") String name) {
        TlsConfiguration config = registry.get(name).orElseThrow();
        if (config.reload()) {
            event.fire(new CertificateUpdatedEvent(name, config));
        } else {
            throw new IllegalStateException("Config " + name + " was not reloaded");
        }
    }
}
