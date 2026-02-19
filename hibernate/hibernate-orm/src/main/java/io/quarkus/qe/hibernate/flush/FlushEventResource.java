package io.quarkus.qe.hibernate.flush;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.Session;

@ApplicationScoped
@Path("/flush-events")
public class FlushEventResource {

    @Named("named")
    @Inject
    Session session;

    private final FlushEventListener flushEventListener = new FlushEventListener();

    @POST
    @Path("/reset")
    public void resetFlushCounter() {
        flushEventListener.resetCounters();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/data")
    public FlushEventListener.FlushEventData getFlushEventData() {
        return flushEventListener.getFlushEventData();
    }

    @Transactional
    @GET
    @Path("/trigger-noop-flush")
    public long triggerNoOpFlush() {
        session.addEventListeners(flushEventListener);
        long count = session.createQuery("SELECT COUNT(e) FROM MyEntity e", Long.class).getSingleResult();
        session.flush();
        return count;
    }
}
