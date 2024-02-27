# Coverage for various GRPC-related things

## Required key generation steps

Based on https://github.com/quarkusio/quarkus-quickstarts/tree/main/grpc-tls-quickstart/src/main/resources/tls

```shell
    # create ca
    openssl req -x509 -new -newkey rsa:2048 -nodes -keyout ca.key -out ca.pem  -config ca.cnf -days 3650 -extensions v3_req -subj "/C=CZ/ST=JMK/L=Brno/O=RedHat/OU=Quarkus-QE"
    # create server keys
    openssl genrsa -out server.key 2048
    openssl req -new -key server.key -out server.csr -subj "/C=CZ/ST=JMK/L=Brno/O=RedHat/OU=Quarkus-QE/CN=localhost"
    openssl x509 -req -CA ca.pem -CAkey ca.key -CAcreateserial -in server.csr -out server.pem -days 3650
    # drop tmp files
    rm ca.key ca.srl server.csr
```