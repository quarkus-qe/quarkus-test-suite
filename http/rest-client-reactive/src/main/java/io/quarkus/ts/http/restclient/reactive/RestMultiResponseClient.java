package io.quarkus.ts.http.restclient.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.client.RestMultiResponse;

@Path("/rest-multi")
@RegisterRestClient(configKey = "rest-multi")
public interface RestMultiResponseClient {
    @GET
    @Path("/objects")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    RestMultiResponse<StreamItem> fetchObjectStream();

    @GET
    @Path("/objects")
    RestMultiResponse<StreamItem> fetchObjectStreamWithoutProduces();

    @GET
    @Path("/strings")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    RestMultiResponse<String> fetchStringStream();

    @GET
    @Path("/header")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    RestMultiResponse<String> fetchStringStreamWithHeader();
}
