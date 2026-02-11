package io.quarkus.ts.cache.infinispan.cdi.request.context;

import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.ts.cache.infinispan.services.RequestScopeService;

@InterceptedRequestContextResponse
@Path("/cache/cdi-request-context")
public class CdiRequestContextResource {

    @Inject
    RequestScopeService requestScopeService;

    @Path("synchronous-response")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CdiRequestContextResponse getSynchronousResponse(@RestQuery String prefix) {
        // here respond with the value from the cached method
        // and JaxRs response filter also enhances this response with the state of CDI request context
        String cacheOutput = requestScopeService.getValueWithPrefix(prefix).result();
        return new CdiRequestContextResponse(cacheOutput, null);
    }

    @Path("async-response")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<CdiRequestContextResponse> getCompletionStageResponse(@RestQuery String prefix) {
        // here respond with the value from the cached method
        // and JaxRs response filter also enhances this response with the state of CDI request context
        return requestScopeService
                .getCompletionStageValueWithPrefix(prefix)
                .thenApply(value -> new CdiRequestContextResponse(value.result(), null));
    }

}
