package io.quarkus.ts.http.restclient.reactive.fault.tolerance;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

/**
 * This server-side filter is used to extract the `REQUEST_ID` header
 * from incoming requests and pass it to the `RequestIdManagerImpl` to manage request context.
 * It also stores the headers via the `HeaderResource` for test verification.
 */
@Provider
@Produces
@PreMatching
@ConstrainedTo(RuntimeType.SERVER)
public class RequestIdContainerRequestFilter implements ContainerRequestFilter {

    @Inject
    RequestIdManagerImpl requestIdManagerImpl;

    @Inject
    HeaderResource headerResource;

    @Override
    @ActivateRequestContext
    public void filter(ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if (headers.containsKey("REQUEST_ID")) {
            int requestId = Integer.valueOf(headers.getFirst("REQUEST_ID"));
            requestIdManagerImpl.overrideRequestId(requestId);
        }

        // Store the headers in the HeaderResource for later retrieval in tests
        headerResource.storeHeaders(headers);
    }
}
