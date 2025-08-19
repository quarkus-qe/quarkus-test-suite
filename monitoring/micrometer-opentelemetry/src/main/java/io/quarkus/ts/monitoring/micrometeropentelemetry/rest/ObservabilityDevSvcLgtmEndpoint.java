package io.quarkus.ts.monitoring.micrometeropentelemetry.rest;

import java.util.Optional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/observability-dev-service-lgtm")
public class ObservabilityDevSvcLgtmEndpoint {

    @ConfigProperty(name = "grafana.endpoint")
    Optional<String> endpoint;

    @GET
    @Path("/grafana-endpoint")
    public String grafanaEndpoint() {
        return endpoint.orElseThrow(
                () -> new WebApplicationException("The 'grafana.endpoint' configuration property is not set", 500));
    }

}
