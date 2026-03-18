package io.quarkus.ts.messaging.kafka.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.reply.KafkaRequestReply;

@ApplicationScoped
@Path("/request")
public class KafkaRequestReplyEmitter {

    @Channel("reqrep")
    KafkaRequestReply<String, String> requestReply;

    @Channel("remessage")
    KafkaRequestReply<String, String> messageRequestReply;

    @GET
    public Uni<String> requestReply(@QueryParam("request") String request) {
        return requestReply.request(request);
    }

    @GET
    @Path("/message")
    public Uni<String> requestMessage(@QueryParam("request") String request) {
        return messageRequestReply.request(Message.of(request)
                .addMetadata(OutgoingKafkaRecordMetadata.builder()
                        .withKey(request)
                        .build())
                .getPayload());
    }
}
