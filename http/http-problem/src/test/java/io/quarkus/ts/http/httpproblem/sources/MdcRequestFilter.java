package io.quarkus.ts.http.httpproblem.sources;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.MDC;

@Provider
public class MdcRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        MDC.put("requestId", "test-request-id-12345");
    }
}
