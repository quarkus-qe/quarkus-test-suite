package io.quarkus.ts.http.restclient.reactive;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "meta-client")
@RegisterClientHeaders
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public interface MetaClient {

    @GET
    @Path("/meta/headers")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getHeaders();
}
