package io.quarkus.ts.http.pact;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.pactfoundation.consumer.dsl.LambdaDsl;
import io.quarkus.test.junit.QuarkusTest;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;

@QuarkusTest
@Tag("QUARKUS-1024")
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "TheProvider", providerType = ProviderType.ASYNCH)
public class ConsumerPactTest {

    @Pact(consumer = "TheConsumer")
    public MessagePact createPact(MessagePactBuilder builder) {
        var body = LambdaDsl.newJsonBody((it) -> {
            it.stringType("payload", "Hello there");
        }).build();

        return builder
                .expectsToReceive("a message with a payload")
                .withMetadata(new HashMap<>())
                .withContent(body)
                .toPact();
    }

    @Test
    void test(List<Message> messages) {
        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals("{\"payload\":\"Hello there\"}", messages.get(0).contentsAsString());
    }
}
