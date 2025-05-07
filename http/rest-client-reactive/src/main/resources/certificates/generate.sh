#!/bin/bash
# this script generates keystore/truststore pair
set -eux
from='1991/08/25'
cert_validity=1
# generate server keystore
keytool -genkey -alias Server -startdate $from -validity $cert_validity -keyalg RSA -keystore server-keystore.p12 -keysize 2048 -storetype PKCS12 -storepass password -dname "cn=localhost, ou=QuarkusQE, o=Redhat, L=Brno, st=BR, c=CZ"
# extract the certificate of the server
keytool -export -keystore server-keystore.p12 -alias Server -file server.crt -storepass password

# create client truststore
keytool -genkey -alias ClientTrust -validity 3650 -keyalg RSA -keystore client-truststore.p12 -keysize 2048 -storetype PKCS12 -storepass password -dname "cn=localhost, ou=QuarkusQE, o=Redhat, L=Brno, st=BR, c=CZ"
# add server certificate into the truststore
keytool -import -keystore client-truststore.p12 -alias Server -file server.crt -storepass password -noprompt
# remove default private key from the trustore
keytool -delete -alias clientTrust -keystore client-truststore.p12 -storepass password

# remove the certificate file
rm server.crt
