package io.quarkus.ts.http.pact;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;

@QuarkusTest
@Tag("QUARKUS-1024")
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "TheProvider", providerType = ProviderType.ASYNCH)
public class ConsumerPactTest {

    @Pact(consumer = "TheConsumer")
    public V4Pact createPact(MessagePactBuilder builder) {
        var body = LambdaDsl.newJsonBody((it) -> {
            it.stringType("payload", "Hello there");
        }).build();

        return builder
                .expectsToReceive("a message with a payload")
                .withMetadata(new HashMap<>())
                .withContent(body)
                .toPact(V4Pact.class);
    }

    @Test
    void test(V4Interaction.AsynchronousMessage messages) {
        Assertions.assertEquals("{\"payload\":\"Hello there\"}", messages.contentsAsString());
    }
}
