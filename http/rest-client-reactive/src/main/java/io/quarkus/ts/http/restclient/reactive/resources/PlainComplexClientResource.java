package io.quarkus.ts.http.restclient.reactive.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
