package io.quarkus.ts.http.restclient.reactive.resources;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.http.restclient.reactive.ResourceAndSubResourcesClient;

@Path("clients/{clientId}/clientResource")
@Consumes("text/plain")
@Produces("text/plain")
public class PlainComplexClientResource {
    @Inject
    @RestClient
    ResourceAndSubResourcesClient resourceAndSubResourcesClient;

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getClientResource(@PathParam("clientId") String rootParam,
            @PathParam("id") String id,
            @QueryParam("baseUri") String baseUri) throws URISyntaxException {

        ResourceAndSubResourcesClient restClient = resourceAndSubResourcesClient;
        if (!StringUtils.isEmpty(baseUri)) {
            restClient = RestClientBuilder.newBuilder().baseUri(new URI(baseUri))
                    .build(ResourceAndSubResourcesClient.class);
        }

        return restClient.clients().get(rootParam).sub().findById(id).retrieve();
    }
}
