package io.quarkus.qe.messaging.ssl.providers;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.common.annotation.Identifier;

public class SslKafkaProvider extends KafkaProviders {

    @ConfigProperty(name = "kafka-client-ssl.bootstrap.servers", defaultValue = "localhost:9092")
    String sslKafkaBootStrap;

    @Inject
    @Identifier("default-kafka-broker")
    Map<String, Object> defaultKafkaBrokerConfig;

    @ApplicationScoped // don't create bean when testing Kafka SASL SSL
    @Produces
    @Named("kafka-consumer-ssl")
    KafkaConsumer<String, String> getSslConsumer() {
        Properties props = setupConsumerProperties(sslKafkaBootStrap);
        sslSetup(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-ssl-consumer"));
        return consumer;
    }

    @ApplicationScoped // don't create bean when testing Kafka SASL SSL
    @Produces
    @Named("kafka-producer-ssl")
    KafkaProducer<String, String> getSslProducer() {
        Properties props = setupProducerProperties(sslKafkaBootStrap);
        sslSetup(props);
        return new KafkaProducer<>(props);
    }

    @ApplicationScoped // don't create bean when testing Kafka SASL SSL
    @Produces
    @Named("kafka-admin-ssl")
    AdminClient getSslAdmin() {
        Properties props = setupConsumerProperties(sslKafkaBootStrap);
        sslSetup(props);
        return KafkaAdminClient.create(props);
    }

    protected void sslSetup(Properties props) {
        props.putAll(defaultKafkaBrokerConfig);
        props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }
}
