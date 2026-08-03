package io.quarkus.ts.http.minimum.reactive.cancelling;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.jboss.resteasy.reactive.server.Cancellable;

@Path("slow/")
public class SlowEndpoint {
    private final static AtomicInteger counter = new AtomicInteger(0);

    @GET
    public String getSlowResponse() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Heeelloooo";
    }

    @GET
    @Path("count")
    public int getCount() {
        return counter.get();
    }

    @Provider
    @Cancellable(value = false)
    public static class CountFilter implements ContainerResponseFilter {
        public CountFilter() {
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
            if (requestContext.getUriInfo().getPath().endsWith("slow")) {
                counter.incrementAndGet();
            }
        }
    }
}
