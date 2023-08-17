# PKCS12 keystore and truststore generation for server and client


1. Create a keystore for the client

`keytool -genkey -alias Client -validity 3650 -keyalg RSA -keystore client-keystore.p12 -keysize 2048 -storetype PKCS12 -storepass password -dname "cn=localhost, ou=QuarkusQE, o=Redhat, L=Brno, st=BR, c=CZ"`

2. Export the public certificate of the client

`keytool -export -keystore client-keystore.p12 -alias Client -file client.crt`

3. Create a keystore for the server

`keytool -genkey -alias Server -validity 3650 -keyalg RSA -keystore server-keystore.p12 -keysize 2048 -storetype PKCS12 -storepass password -dname "cn=localhost, ou=QuarkusQE, o=Redhat, L=Brno, st=BR, c=CZ"`

4. Export the public certificate of the server

`keytool -export -keystore server-keystore.p12 -alias Server -file server.crt`

5. Create a truststore for the client

`keytool -genkey -alias ClientTrust -validity 3650 -keyalg RSA -keystore client-truststore.p12 -keysize 2048 -storetype PKCS12 -storepass password -dname "cn=localhost, ou=QuarkusQE, o=Redhat, L=Brno, st=BR, c=CZ"`

6. Create a truststore for the server

`keytool -genkey -alias ServerTrust -validity 3650 -keyalg RSA -keystore server-truststore.p12 -keysize 2048 -storetype PKCS12 -storepass password -dname "cn=localhost, ou=QuarkusQE, o=Redhat, L=Brno, st=BR, c=CZ"`

7. Import the client public certificate into the server truststore

`keytool -import -keystore server-truststore.p12 -alias Client -file client.crt`

8. Import the server public certificate into the client truststore

`keytool -import -keystore client-truststore.p12 -alias Server -file server.crt`

9. Delete the existing private key of the server truststore

`keytool -delete -alias serverTrust -keystore server-truststore.p12 -storepass password`

10. Delete the existing private key of the client truststore

`keytool -delete -alias clientTrust -keystore client-truststore.p12 -storepass password`
