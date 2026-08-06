package io.quarkus.ts.http.restclient.reactive.resources;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ResponseHeader;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.client.BasicRestResponse;
import org.jboss.resteasy.reactive.client.RestMultiResponse;

import io.quarkus.ts.http.restclient.reactive.RestMultiResponseClient;
import io.quarkus.ts.http.restclient.reactive.StreamItem;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/rest-multi")
public class RestMultiResponseResource {
    @Inject
    @RestClient
    RestMultiResponseClient client;

    @GET
    @Path("/objects")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<StreamItem> streamObjects() {
        return Multi.createFrom().items(new StreamItem("one"), new StreamItem("two"));
    }

    @GET
    @Path("/strings")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> streamStrings() {
        return Multi.createFrom().items("one", "two");
    }

    @GET
    @Path("/header")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @ResponseHeader(name = "Custom-Header", value = "hello")
    public Multi<String> streamStringsWithHeader() {
        return Multi.createFrom().items("one", "two");
    }

    @GET
    @Path("/verify/objects")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<List<StreamItem>>> verifyObjects() {
        return verifyObjectResponse(client.fetchObjectStream());
    }

    @GET
    @Path("/verify/objects-without-produces")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<List<StreamItem>>> verifyObjectsWithoutProduces() {
        return verifyObjectResponse(client.fetchObjectStreamWithoutProduces());
    }

    @GET
    @Path("/verify/strings")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<List<String>>> verifyStrings() {
        return verifyStringResponse(client.fetchStringStream());
    }

    @GET
    @Path("/verify/header")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<List<String>>> verifyHeader() {
        return verifyStringResponse(client.fetchStringStreamWithHeader());
    }

    private Uni<RestResponse<List<StreamItem>>> verifyObjectResponse(RestMultiResponse<StreamItem> streamResponse) {
        Uni<BasicRestResponse> responseMetadata = streamResponse.response();
        Uni<List<StreamItem>> collectedItems = streamResponse.collect().asList();

        return Uni.combine().all().unis(responseMetadata, collectedItems)
                .asTuple()
                .map(combinedResult -> {
                    BasicRestResponse metadata = combinedResult.getItem1();
                    List<StreamItem> items = combinedResult.getItem2();

                    return RestResponse.ResponseBuilder
                            .ok(items)
                            .header("Rest-Client-Status", metadata.status())
                            .build();
                });
    }

    private Uni<RestResponse<List<String>>> verifyStringResponse(RestMultiResponse<String> streamResponse) {
        Uni<BasicRestResponse> responseMetadata = streamResponse.response();
        Uni<List<String>> collectedItems = streamResponse.collect().asList();

        return Uni.combine().all().unis(responseMetadata, collectedItems)
                .asTuple()
                .map(combinedResult -> {
                    BasicRestResponse metadata = combinedResult.getItem1();
                    List<String> items = combinedResult.getItem2();

                    var response = RestResponse.ResponseBuilder
                            .ok(items)
                            .header("Rest-Client-Status", metadata.status());

                    String customHeaderValue = metadata.headers().getFirst("Custom-Header");

                    if (customHeaderValue != null) {
                        response.header("Custom-Header", customHeaderValue);
                    }

                    return response.build();
                });
    }
}
