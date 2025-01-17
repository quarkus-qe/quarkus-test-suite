package io.quarkus.ts.messaging.kafka.producer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnRHBQandWindows;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.DockerContainerManagedResource;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.buffer.Buffer;

@QuarkusScenario
@DisabledOnRHBQandWindows(reason = "https://issues.redhat.com/browse/QUARKUS-5214")
public class SnappyCompressionIT {

    private static final int TIMEOUT_SEC = 5;

    private static final String BIG_JSON = "/big_json.json";

    private static final String COMMAND_LOG_CONTAINER = "./bin/kafka-run-class.sh kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files /tmp/default-log-dir/test-0/00000000000000000000.log | head";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
    static final KafkaService kafka = new KafkaService().withProperty("auto.create.topics.enable", "false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.snappy.enabled", "true")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void checkCompressCodecSnappy() throws IOException, InterruptedException {
        String msg = "This is the message";
        GenericContainer<?> container = kafka.getPropertyFromContext(DockerContainerManagedResource.DOCKER_INNER_CONTAINER);
        UniAssertSubscriber<Object> subscriber = makeHttpReqWithMessage("/messageEvent", msg)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();
        String logSegmentHead = container.execInContainer("bash", "-c", COMMAND_LOG_CONTAINER)
                .getStdout();
        Assertions.assertTrue(logSegmentHead.contains("compresscodec: snappy"));
    }

    @Test
    public void checkSnappyCompressionSize() throws IOException, InterruptedException {
        GenericContainer<?> container = kafka.getPropertyFromContext(DockerContainerManagedResource.DOCKER_INNER_CONTAINER);
        String largeMessage = "This is the large message".repeat(500);
        int originalMessageSize = largeMessage.length();
        UniAssertSubscriber<Object> subscriber = makeHttpReqWithMessage("/messageEvent", largeMessage)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();
        String logSegmentHead = container.execInContainer("bash", "-c", COMMAND_LOG_CONTAINER)
                .getStdout();
        int compressedMessageSize = extractCompressedSize(logSegmentHead);
        Assertions.assertTrue(compressedMessageSize < originalMessageSize,
                "Compressed message size should be smaller than the original size");
    }

    @Test
    public void checkIntegrityMessageAfterCompression() {
        String msg = "This is the message";
        try (var consumer = createConsumer()) {
            UniAssertSubscriber<Object> subscriber = makeHttpReqWithMessage("/messageEvent", msg)
                    .subscribe().withSubscriber(UniAssertSubscriber.create());
            subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();

            ConsumerRecord<Integer, String> records = consumer.poll(Duration.ofMillis(10000))
                    .iterator().next();
            Assertions.assertEquals(records.value(), msg);
        }
    }

    @Test
    public void sendBigJsonAndVerifyCompression() throws IOException, InterruptedException {
        GenericContainer<?> container = kafka.getPropertyFromContext(DockerContainerManagedResource.DOCKER_INNER_CONTAINER);
        int originalJsonSize = calculateOriginalJsonLength();
        String largeJson = loadLargeJsonFromFile(BIG_JSON);
        UniAssertSubscriber<Object> subscriber = makeHttpReqWithMessage("/messageEvent", largeJson)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();

        String logSegmentHead = container.execInContainer("bash", "-c", COMMAND_LOG_CONTAINER)
                .getStdout();
        int compressedMessageSize = extractCompressedSize(logSegmentHead);
        Assertions.assertTrue(compressedMessageSize < originalJsonSize,
                "Compressed message size should be smaller than the original size");
    }

    private KafkaConsumer<Integer, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapUrl());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<Integer, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test"));
        return consumer;
    }

    private int extractCompressedSize(String logSegmentHead) throws IOException {

        Pattern pattern = Pattern.compile("size: (\\d+)");
        Matcher matcher = pattern.matcher(logSegmentHead);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IOException("Failed to extract compressed size from log segment head: " + logSegmentHead);
        }
    }

    private String loadLargeJsonFromFile(String filePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new FileNotFoundException("File not found: " + filePath);
            }
        }
    }

    private int calculateOriginalJsonLength() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(BIG_JSON)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + BIG_JSON);
            }
            byte[] jsonBytes = inputStream.readAllBytes();
            return jsonBytes.length;
        }
    }

    private Uni<Void> makeHttpReqWithMessage(String path, String message) {
        Buffer buffer = Buffer.buffer(message);
        return app.mutiny().postAbs(getAppEndpoint() + path).sendBuffer(buffer)
                .replaceWithVoid();
    }

    private String getAppEndpoint() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
