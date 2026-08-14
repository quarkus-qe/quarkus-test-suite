The keystores in src/main/resources/ were generated following a similar approach used in the `rest-client-reactive` module.

# How the certificates were generated
Two client keystores are used: client-good.p12 (trusted by the server) and client-bad.p12 (not trusted). Only the good client's certificate is imported into the server truststore, so mTLS succeeds for client-good and fails for client-bad.

# Generate client keystores
keytool -genkey -alias ClientGood -keyalg RSA -validity 3650 -storetype PKCS12 -keystore client-good.p12 -storepass password -keypass password -dname "CN=client-good"

keytool -genkey -alias ClientBad  -keyalg RSA -validity 3650 -storetype PKCS12 -keystore client-bad.p12  -storepass password -keypass password -dname "CN=client-bad"

# Add client-good to the server truststore
keytool -export -keystore client-good.p12 -alias ClientGood -storepass password -file client-good.crt
keytool -import -keystore server-truststore.p12 -storetype PKCS12 -storepass password -alias ClientGood -file client-good.crt -noprompt
rm client-good.crt