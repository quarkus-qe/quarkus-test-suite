package io.quarkus.ts.http.restclient.reactive;

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

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestPath;

@Path("/plain-root/{rootParam}")
@Consumes("text/plain")
@Produces("text/plain")
public class PlainRootResource {

    @Inject
    @RestClient
    SubResourcesClient subResourcesClient;

    @GET
    @Path("{method}/{param2}/{param3}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSubResource(
            @RestPath("rootParam") String rootParam,
            @RestPath("method") String method,
            @RestPath("param2") String param2,
            @RestPath("param3") String param3) {
        return subResourcesClient.sub(rootParam, method).sub(param2).get(param3);
    }

    @GET
    @Path("/manualClient/{method}/{param2}/{param3}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSubResourceManualClient(
            @PathParam("rootParam") String rootParam,
            @PathParam("method") String method,
            @PathParam("param2") String param2,
            @PathParam("param3") String param3,
            @QueryParam("baseUri") String baseUri) throws URISyntaxException {
        SubResourcesClient rClient = RestClientBuilder.newBuilder().baseUri(new URI(baseUri)).build(SubResourcesClient.class);
        return rClient.sub(rootParam, method).sub(param2).get(param3);
    }
}
