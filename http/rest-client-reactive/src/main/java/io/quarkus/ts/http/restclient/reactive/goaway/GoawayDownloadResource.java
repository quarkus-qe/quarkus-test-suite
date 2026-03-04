package io.quarkus.ts.http.restclient.reactive.goaway;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/goaway")
public class GoawayDownloadResource {

    @Inject
    @RestClient
    GoawayServerClient serverClient;

    @GET
    @Path("/download")
    @Produces(MediaType.TEXT_PLAIN)
    public String download() {
        try (Response response = serverClient.download()) {
            byte[] data = response.readEntity(byte[].class);
            return data.length + " bytes downloaded successfully";
        }

    }
}
