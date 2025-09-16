package io.quarkus.ts.messaging.kafka.restclient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
@RegisterRestClient(configKey = "signal-uppercase-rest-client")
@Path("/uppercase")
@Timeout(60000)
public interface UppercaseRestClient {

    @GET
    Uni<String> toUppercase(@QueryParam("source") String source);
}
