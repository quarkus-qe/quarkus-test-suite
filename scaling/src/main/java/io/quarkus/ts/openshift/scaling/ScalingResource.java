package io.quarkus.ts.openshift.scaling;

import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;

@Path("/scaling")
public class ScalingResource {

    private static final Logger LOG = Logger.getLogger(ScalingResource.class.getName());

    private Long id;

    void startup(@Observes StartupEvent event) {
        id = System.currentTimeMillis();
        LOG.info("ScalingResource Up! | ID: " + id);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Long get() {
        return id;
    }
}
