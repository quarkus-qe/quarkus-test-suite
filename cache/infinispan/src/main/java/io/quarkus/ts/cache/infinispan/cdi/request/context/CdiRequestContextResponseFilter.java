package io.quarkus.ts.cache.infinispan.cdi.request.context;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerResponseContext;

import org.jboss.resteasy.reactive.server.ServerResponseFilter;

public class CdiRequestContextResponseFilter {

    @Inject
    RequestContextState requestContextState;

    @InterceptedRequestContextResponse
    @ServerResponseFilter
    public void filter(ContainerResponseContext responseContext) {
        String requestContextResponse = requestContextState.getState();
        final String cacheResponse;
        if (responseContext.getEntity() instanceof CdiRequestContextResponse response) {
            cacheResponse = response.cacheOutput();
        } else {
            throw new IllegalStateException("Unsupported response type: " + responseContext.getEntity());
        }

        CdiRequestContextResponse jointResponse = new CdiRequestContextResponse(cacheResponse, requestContextResponse);
        responseContext.setStatus(200);
        responseContext.setEntity(jointResponse);
    }

}
