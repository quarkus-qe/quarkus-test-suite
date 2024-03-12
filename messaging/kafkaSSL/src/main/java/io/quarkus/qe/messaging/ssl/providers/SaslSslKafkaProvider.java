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
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class SaslSslKafkaProvider extends KafkaProviders {

    private final static String SASL_USERNAME_VALUE = "client";
    private final static String SASL_PASSWORD_VALUE = "client-secret";

    @ConfigProperty(name = "kafka-client-sasl-ssl.bootstrap.servers", defaultValue = "localhost:9092")
    String saslSslKafkaBootStrap;

    @ConfigProperty(name = "kafka.ssl.truststore.location", defaultValue = "server.jks")
    String trustStoreFile;

    @ConfigProperty(name = "kafka.ssl.truststore.password", defaultValue = "top-secret")
    String trustStorePassword;

    @ConfigProperty(name = "kafka.ssl.truststore.type", defaultValue = "PKCS12")
    String trustStoreType;

    @Singleton
    @Produces
    @Named("kafka-consumer-sasl-ssl")
    KafkaConsumer<String, String> getSaslSslConsumer() {
        Properties props = setupConsumerProperties(saslSslKafkaBootStrap);
        saslSetup(props);
        sslSetup(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-sasl-ssl-consumer"));
        return consumer;
    }

    @Singleton
    @Produces
    @Named("kafka-producer-sasl-ssl")
    KafkaProducer<String, String> getSaslSslProducer() {
        Properties props = setupProducerProperties(saslSslKafkaBootStrap);
        saslSetup(props);
        sslSetup(props);
        return new KafkaProducer<>(props);
    }

    @Singleton
    @Produces
    @Named("kafka-admin-sasl-ssl")
    AdminClient getSaslSslAdmin() {
        Properties props = setupConsumerProperties(saslSslKafkaBootStrap);
        saslSetup(props);
        sslSetup(props);
        return KafkaAdminClient.create(props);
    }

    private static void saslSetup(Properties props) {
        props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.setProperty(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"" + SASL_USERNAME_VALUE + "\" "
                        + "password=\"" + SASL_PASSWORD_VALUE + "\";");
    }

    protected void sslSetup(Properties props) {
        File tsFile = new File(trustStoreFile);
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, tsFile.getPath());
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, trustStoreType);
        props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }
}
