package io.quarkus.ts.messaging.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;

public class CustomStrimziKafkaContainer extends GenericContainer<CustomStrimziKafkaContainer> {

    protected static final String LATEST_KAFKA_VERSION = "2.7.0";
    protected static final String STARTER_SCRIPT = "/testcontainers_start.sh";
    protected static final int KAFKA_PORT = 9092;
    protected static final int ZOOKEEPER_PORT = 2181;

    private int kafkaExposedPort;
    Map<String, String> kafkaServerProp;

    public CustomStrimziKafkaContainer(final String version, final Map<String, String> kafkaServerProp) {
        super("quay.io/strimzi/kafka:" + version);
        super.withNetwork(Network.SHARED);
        this.kafkaServerProp = kafkaServerProp;
        // exposing kafka port from the container
        withExposedPorts(KAFKA_PORT);

        withEnv("LOG_DIR", "/tmp");
    }

    @Override
    public void doStart() {
        withCommand("sh", "-c", "while [ ! -f " + STARTER_SCRIPT + " ]; do sleep 0.1; done; " + STARTER_SCRIPT);
        super.doStart();
    }

    public CustomStrimziKafkaContainer(final Map<String, String> kafkaServerProp) {
        this("latest-kafka-" + LATEST_KAFKA_VERSION, kafkaServerProp);
    }

    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo, boolean reused) {
        super.containerIsStarting(containerInfo, reused);

        kafkaExposedPort = getMappedPort(KAFKA_PORT);

        StringBuilder advertisedListeners = new StringBuilder(getBootstrapServers());

        Collection<ContainerNetwork> cns = containerInfo.getNetworkSettings().getNetworks().values();

        for (ContainerNetwork cn : cns) {
            advertisedListeners.append("," + "BROKER://").append(cn.getIpAddress()).append(":9093");
        }

        String command = "#!/bin/bash \n";
        command += "bin/zookeeper-server-start.sh config/zookeeper.properties &\n";
        command += "bin/kafka-server-start.sh config/server.properties --override listeners=PLAINTEXT://0.0.0.0:9092,BROKER://0.0.0.0:9093"
                +
                " --override advertised.listeners=" + advertisedListeners +
                " --override zookeeper.connect=localhost:" + ZOOKEEPER_PORT +
                " --override listener.security.protocol.map=PLAINTEXT:PLAINTEXT,BROKER:PLAINTEXT" +
                " --override inter.broker.listener.name=BROKER";

        for (Map.Entry<String, String> entry : kafkaServerProp.entrySet()) {
            command += " --override " + entry.getKey() + "=" + entry.getValue() + "\n";
        }

        copyFileToContainer(
                Transferable.of(command.getBytes(StandardCharsets.UTF_8), 700),
                STARTER_SCRIPT);
    }

    public String getBootstrapServers() {
        return String.format("PLAINTEXT://%s:%s", getContainerIpAddress(), kafkaExposedPort);
    }
}
