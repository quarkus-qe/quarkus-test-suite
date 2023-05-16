package io.quarkus.ts.opentelemetry.reactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/scheduler")
public class SchedulerResource {

    @Inject
    SchedulerService schedulerService;

    @Path("/count")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public int getCount() {
        return schedulerService.getCount();
    }

}
