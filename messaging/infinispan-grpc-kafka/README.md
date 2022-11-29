# Table of Contents
1. [Quarkus infinispan grpc kafka](#Quarkus-infinispan-grpc-kafka)
2. [Quarkus SSL/TLS Infinispan scenario ](#Quarkus-SSL/TLS-Infinispan-scenario )
3. [Quarkus Grateful Shutdown for Kafka connectors](#Quarkus-Grateful-Shutdown-for-Kafka-connectors)

## Quarkus infinispan grpc kafka
Module that test whether gRPC, Infinispan and Kafka extensions work together:
- for gRPC: there is a simple greetings endpoint. This example will use a `helloworld.proto` file to generate the required sources. 
- for Infinispan: to check whether the cache persistence is working fine
- for Kafka: to verify the messages are working in a chain workflow.

## Kafka-client SASL
Verifies SASL authentication through Quarkus Kafka client extension. 
Endpoint `SaslKafkaEndpoint` is able to produce events
and consume events and check topics through `AdminClient` and `KafkaConsumer`.

## Quarkus Infinispan scenario 

##Infinispan server SSL/TLS
TrustStore is used to store certificates from Certified Authorities (CA) that verify the certificate presented by the server 
in an SSL connection. While Keystore is used to store private key and identity certificates that a specific program should present to 
both parties (server or client) for verification.

Security infinispan documentation:
- https://infinispan.org/docs/stable/titles/server/server.html#authentication-mechanisms
- https://infinispan.org/docs/stable/titles/server/server.html#security-realms

We have used the following commands in order to generate the required certificates.

Create the Keystore certificate
```shell
keytool -v -genkeypair -keyalg RSA -dname "cn=Quarkus, ou=Quarkus, o=Redhat, L=San Francisco, st=CA, c=US" -ext SAN="DNS:localhost,IP:127.0.0.1" -validity 3825 -alias 1 -keystore keystore.jks -keypass password -storepass password
```

Export the Certificate to add it into Truststore
```shell
keytool -export -alias 1 -file localhost.cer -keystore keystore.jks -storepass password
```

Create a Trustore certificate
```shell
keytool -import -v -trustcacerts -alias 1 -file localhost.cer -keystore truststore.jks -storepass password
```
