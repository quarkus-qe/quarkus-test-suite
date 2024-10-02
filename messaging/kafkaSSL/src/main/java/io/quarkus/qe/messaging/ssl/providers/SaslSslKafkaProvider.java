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

public class SaslSslKafkaProvider extends KafkaProviders {

    @ConfigProperty(name = "kafka-client-sasl-ssl.bootstrap.servers", defaultValue = "localhost:9092")
    String saslSslKafkaBootStrap;

    @Inject
    @Identifier("default-kafka-broker")
    Map<String, Object> defaultKafkaBrokerConfig;

    @ApplicationScoped // don't create bean when testing Kafka SSL
    @Produces
    @Named("kafka-consumer-sasl-ssl")
    KafkaConsumer<String, String> getSaslSslConsumer() {
        Properties props = setupConsumerProperties(saslSslKafkaBootStrap);
        saslSslSetup(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-sasl-ssl-consumer"));
        return consumer;
    }

    @ApplicationScoped // don't create bean when testing Kafka SSL
    @Produces
    @Named("kafka-producer-sasl-ssl")
    KafkaProducer<String, String> getSaslSslProducer() {
        Properties props = setupProducerProperties(saslSslKafkaBootStrap);
        saslSslSetup(props);
        return new KafkaProducer<>(props);
    }

    @ApplicationScoped // don't create bean when testing Kafka SSL
    @Produces
    @Named("kafka-admin-sasl-ssl")
    AdminClient getSaslSslAdmin() {
        Properties props = setupConsumerProperties(saslSslKafkaBootStrap);
        saslSslSetup(props);
        return KafkaAdminClient.create(props);
    }

    protected void saslSslSetup(Properties props) {
        props.putAll(defaultKafkaBrokerConfig);
        props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }
}
