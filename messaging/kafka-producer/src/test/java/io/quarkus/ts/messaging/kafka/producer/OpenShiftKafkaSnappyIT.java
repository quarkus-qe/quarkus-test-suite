package io.quarkus.ts.messaging.kafka.producer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.buffer.Buffer;

@OpenShiftScenario
public class OpenShiftKafkaSnappyIT {

    private static final int TIMEOUT_SEC = 5;
    private static final String FILTER_COMMAND_LOG_CONTAINER = "/bin/bash -c \"./bin/kafka-run-class.sh kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files /tmp/kraft-combined-logs/test-0/00000000000000000000.log | head\"";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
    static final KafkaService kafka = new KafkaService().withProperty("auto.create.topics.enable", "false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.snappy.enabled", "true")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void checkCompressCodecSnappy() throws IOException, InterruptedException {
        String msg = "This is the message";

        UniAssertSubscriber<Object> subscriber = makeHttpReqWithMessage("/messageEvent", msg)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();

        ProcessBuilder pbGetPods = new ProcessBuilder("oc", "get", "pods", "-l", "app=kafka", "-o",
                "jsonpath={.items[0].metadata.name}");
        Process processGetPods = pbGetPods.start();
        String kafkaPodName = new BufferedReader(new InputStreamReader(processGetPods.getInputStream())).readLine().trim();

        ProcessBuilder pbExecCommand = new ProcessBuilder("oc", "rsh", kafkaPodName, "bash", "-c",
                FILTER_COMMAND_LOG_CONTAINER);
        Process processExecCommand = pbExecCommand.start();
        processExecCommand.waitFor();
        String logSegmentHead = new String(processExecCommand.getInputStream().readAllBytes());

        Assertions.assertTrue(logSegmentHead.contains("compresscodec: snappy"));
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
