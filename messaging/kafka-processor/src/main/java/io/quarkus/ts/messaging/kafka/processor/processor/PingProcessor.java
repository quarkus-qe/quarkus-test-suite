package io.quarkus.ts.messaging.kafka.processor.processor;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;

import io.quarkiverse.kafkastreamsprocessor.api.Processor;

@Processor
public class PingProcessor extends ContextualProcessor<String, String, String, String> {

    @Override
    public void process(Record<String, String> ping) {
        context().forward(ping);
    }
}
