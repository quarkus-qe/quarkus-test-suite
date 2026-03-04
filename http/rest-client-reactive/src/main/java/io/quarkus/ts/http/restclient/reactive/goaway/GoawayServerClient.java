package io.quarkus.ts.http.restclient.reactive.goaway;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "goaway-file-server")
@Path("/goaway-server")
public interface GoawayServerClient {

    @GET
    @Path("/file")
    Response download();

}
