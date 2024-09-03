package io.quarkus.ts.messaging.kafka.reactive.streams.shutdown;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/slow-topic")
public class SlowTopicResource {

    @Inject
    @Channel("slow-topic")
    Emitter<String> emitter;

    @POST
    @Path("/sendMessages/{count}")
    public void sendMessage(@PathParam("count") Integer count) {
        for (int index = 1; index <= count; index++) {
            emitter.send("Message " + index);
        }
    }

    @POST
    @Path("/sendMessage/{content}")
    public Uni<Void> sendMessage(@PathParam("content") String message) {
        return Uni.createFrom().completionStage(emitter.send(message));
    }

}
