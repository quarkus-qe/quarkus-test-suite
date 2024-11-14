# Table of Contents
1. [Quarkus infinispan grpc kafka](#Quarkus-infinispan-grpc-kafka)
2. [Kafka-client SSL SASL](#Kafka-client-SSL-SASL)

## Quarkus infinispan grpc kafka
Module that test whether gRPC, Infinispan and Kafka extensions work together:
- for gRPC: there is a simple greetings endpoint. This example will use a `helloworld.proto` file to generate the required sources. 
- for Infinispan: to check whether the cache persistence is working fine
- for Kafka: to verify the messages are working in a chain workflow.

## Kafka-client SSL SASL
Verifies SASL authentication through Quarkus Kafka client extension. 
Endpoint `SaslSslKafkaEndpoint` is able to produce events
and consume events and check topics through `AdminClient` and `KafkaConsumer`.

