package io.quarkus.qe.messaging.ssl.providers;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class SslKafkaProvider extends KafkaProviders {

    @ConfigProperty(name = "kafka-client-ssl.bootstrap.servers", defaultValue = "localhost:9092")
    String sslKafkaBootStrap;

    @ConfigProperty(name = "kafka.ssl.truststore.location", defaultValue = "server.jks")
    String trustStoreFile;

    @ConfigProperty(name = "kafka.ssl.truststore.password", defaultValue = "top-secret")
    String trustStorePassword;

    @ConfigProperty(name = "kafka.ssl.truststore.type", defaultValue = "PKCS12")
    String trustStoreType;

    @Singleton
    @Produces
    @Named("kafka-consumer-ssl")
    KafkaConsumer<String, String> getSslConsumer() {
        Properties props = setupConsumerProperties(sslKafkaBootStrap);
        sslSetup(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-ssl-consumer"));
        return consumer;
    }

    @Singleton
    @Produces
    @Named("kafka-producer-ssl")
    KafkaProducer<String, String> getSslProducer() {
        Properties props = setupProducerProperties(sslKafkaBootStrap);
        sslSetup(props);
        return new KafkaProducer<>(props);
    }

    @Singleton
    @Produces
    @Named("kafka-admin-ssl")
    AdminClient getSslAdmin() {
        Properties props = setupConsumerProperties(sslKafkaBootStrap);
        sslSetup(props);
        return KafkaAdminClient.create(props);
    }

    protected void sslSetup(Properties props) {
        File tsFile = new File(trustStoreFile);
        props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, tsFile.getPath());
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, trustStoreType);
        props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }
}
