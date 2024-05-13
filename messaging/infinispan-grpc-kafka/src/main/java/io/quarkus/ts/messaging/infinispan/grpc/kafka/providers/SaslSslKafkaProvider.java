package io.quarkus.ts.messaging.infinispan.grpc.kafka.providers;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

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
    private final static String SASL_PASSWORD_VALUE = "client-secret12345678912345678912";

    @ConfigProperty(name = "kafka.ssl.truststore.location")
    Optional<String> trustStoreFile;

    @ConfigProperty(name = "kafka.ssl.truststore.password")
    Optional<String> trustStorePassword;

    @ConfigProperty(name = "kafka.ssl.truststore.type")
    Optional<String> trustStoreType;

    @ConfigProperty(name = "kafka-client-sasl-ssl.bootstrap.servers")
    Optional<String> saslSslKafkaBootStrap;

    @ApplicationScoped
    @Produces
    @Named("kafka-consumer-sasl-ssl")
    KafkaConsumer<String, String> getSaslSslConsumer() {
        Properties props = setupConsumerProperties(saslSslKafkaBootStrap.get());
        saslSetup(props);
        sslSetup(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("test-ssl-consumer"));
        return consumer;
    }

    @ApplicationScoped
    @Produces
    @Named("kafka-producer-sasl-ssl")
    KafkaProducer<String, String> getSaslSslProducer() {
        Properties props = setupProducerProperties(saslSslKafkaBootStrap.get());
        saslSetup(props);
        sslSetup(props);
        return new KafkaProducer<>(props);
    }

    @ApplicationScoped
    @Produces
    @Named("kafka-admin-sasl-ssl")
    AdminClient getSaslSslAdmin() {
        Properties props = setupConsumerProperties(saslSslKafkaBootStrap.get());
        saslSetup(props);
        sslSetup(props);
        return KafkaAdminClient.create(props);
    }

    private static void saslSetup(Properties props) {
        props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.setProperty(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        props.setProperty(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
                        .formatted(SASL_USERNAME_VALUE, SASL_PASSWORD_VALUE));
    }

    protected void sslSetup(Properties props) {
        File tsFile = new File(trustStoreFile.get());
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, tsFile.getPath());
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword.get());
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, trustStoreType.get());
        props.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
    }
}
