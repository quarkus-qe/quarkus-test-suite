package io.quarkus.ts.http.advanced.reactive;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.ws.rs.sse.SseEventSource;

import org.eclipse.microprofile.config.ConfigProvider;

@Path("sse")
public class SseEventUpdateResource {
    public static final String DATA_VALUE = "random data value";

    @Context
    Sse sse;

    @GET
    @Path("client-update")
    @Produces(MediaType.TEXT_PLAIN)
    public Response clientUpdate() throws InterruptedException {
        String host = ConfigProvider.getConfig().getValue("quarkus.http.host", String.class);
        int port = ConfigProvider.getConfig().getValue("quarkus.http.port", Integer.class);
        List<String> receivedData = new CopyOnWriteArrayList<>();

        WebTarget target = ClientBuilder.newClient().target("http://" + host + ":" + port + "/api/sse/server-update");
        try (SseEventSource eventSource = SseEventSource.target(target).build()) {
            eventSource.register(ev -> {
                String event = "event: name=" + ev.getName() + " data={" + ev.readData() + "} and is empty: " + ev.isEmpty()
                        + "\n";
                receivedData.add(event);
            }, thr -> {
                String event = "Error: " + thr.getMessage() + "\n" + Arrays.toString(thr.getStackTrace());
                receivedData.add(event);
            });

            CountDownLatch latch = new CountDownLatch(2);
            eventSource.open();
            latch.await(1, TimeUnit.SECONDS);
        }
        return Response.ok(String.join("\n", receivedData)).build();
    }

    @GET
    @Path("server-update")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void updates(@Context SseEventSink eventSink) {
        eventSink.send(createEvent("NON EMPTY", DATA_VALUE));
        eventSink.send(createEvent("EMPTY", ""));
    }

    private OutboundSseEvent createEvent(String name, String data) {
        return sse.newEventBuilder()
                .name(name)
                .data(data)
                .build();
    }
}
