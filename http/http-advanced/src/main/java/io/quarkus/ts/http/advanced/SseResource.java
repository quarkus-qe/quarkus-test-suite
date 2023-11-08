package io.quarkus.ts.http.advanced;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseEventSource;

@Path("/sse")
public class SseResource {
    @Context
    Sse sse;

    @GET
    @Path("/client")
    public String sseClient() {
        try {
            return consumeSse();
        }
        // in case that https://github.com/quarkusio/quarkus/issues/36402 throws java.lang.RuntimeException: java.lang.ClassNotFoundException:
        // catch it and return the error message
        catch (RuntimeException exception) {
            return exception.getMessage();
        }
    }

    private String consumeSse() {
        StringBuilder response = new StringBuilder();

        /*
         * Client connects to itself (to server endpoint running on same app),
         * because for https://github.com/quarkusio/quarkus/issues/36402 to reproduce client must run on native app.
         * Which cannot be done in test code itself.
         * This method acts just as a client
         */
        WebTarget target = ClientBuilder.newClient().target("http://localhost:" + getQuarkusPort() + "/api/sse/server");
        SseEventSource updateSource = SseEventSource.target(target).build();
        updateSource.register(ev -> {
            response.append("event: ").append(ev.getName()).append(" ").append(ev.readData());
            response.append("\n");

        }, thr -> {
            response.append("Error in SSE, message: ").append(thr.getMessage()).append("\n");
            response.append(Arrays.toString(thr.getStackTrace()));
        });
        updateSource.open();

        LockSupport.parkNanos(2_000_000_000L);
        return response.toString();
    }

    /**
     * Test runner assigns random ports, on which the app should run.
     * Parse this port from the CLI and return it.
     * If no parameter is specified, return the default (8080)
     *
     * @return port on which the application is running
     */
    private int getQuarkusPort() {
        String value = System.getProperty("quarkus.http.port");
        if (value == null || value.isEmpty()) {
            return 8080;
        }
        return Integer.parseInt(value);
    }

    @GET
    @Path("/server")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void sendSseEvents(@Context SseEventSink eventSink) {
        eventSink.send(createEvent("test234", "test"));
        LockSupport.parkNanos(1_000_000_000L);
    }

    private OutboundSseEvent createEvent(String name, String data) {
        return sse.newEventBuilder()
                .name(name)
                .data(data)
                .build();
    }
}
