The following script (with different `from` dates and different validity lengths) was used to generate keystores and truststores.
It creates files `server-keystore.p12 ` and `client-truststore.p12` which were renamed afterwards to be used in TLSRegistryIT.
```
#!/bin/bash
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
```

Certificates in `keystore.p12` and `truststore.p12` have validity of 3650 days (10 years) starting from 7.5.2025.
The rest has validity of one day starting from 25.8.1991 (`ancient` keystore), 1.5.2025 (`old` keystore) and 7.5.2125 (`future` keystore)



