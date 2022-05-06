# BouncyCastle FIPS / JSSE

## Generate certificates steps (Keytool manual process)

```shell
keytool -keystore server-keystore.jks -alias localhost -validity 3000 -genkeypair -keyalg RSA -keysize 2048 -storepass password -keypass password -storetype BCFKS -providerpath /home/pablojosegonzalezgranados/.m2/repository/org/bouncycastle/bc-fips/1.0.2.1/bc-fips-1.0.2.1.jar -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -dname "cn=Redhat, ou=QuarkusQE, o=Redhat,L=Madrid, st=MA, c=ES" -ext SAN="DNS:localhost,IP:127.0.0.1"
openssl req -new -x509 -keyout ca-key -out ca-cert -days 3000
keytool -keystore client-truststore.jks -storetype BCFKS -alias CARoot -import -file ca-cert -providerpath /home/pablojosegonzalezgranados/.m2/repository/org/bouncycastle/bc-fips/1.0.2.1/bc-fips-1.0.2.1.jar -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
keytool -keystore server-truststore.jks -storetype BCFKS -alias CARoot -importcert -file ca-cert -providerpath /home/pablojosegonzalezgranados/.m2/repository/org/bouncycastle/bc-fips/1.0.2.1/bc-fips-1.0.2.1.jar -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
keytool -keystore server-keystore.jks -alias localhost -storepass password -keypass password -storetype BCFKS -certreq -file cert-file -providerpath /home/pablojosegonzalezgranados/.m2/repository/org/bouncycastle/bc-fips/1.0.2.1/bc-fips-1.0.2.1.jar -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 3000 -CAcreateserial -passin pass:password
keytool -keystore server-keystore.jks -storetype BCFKS -alias CARoot -import -file ca-cert -storepass password -providerpath /home/pablojosegonzalezgranados/.m2/repository/org/bouncycastle/bc-fips/1.0.2.1/bc-fips-1.0.2.1.jar -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
cat ca-cert cert-signed > cert
keytool -keystore server-keystore.jks -storetype BCFKS -storepass password -alias localhost -import -file cert -providerpath /home/pablojosegonzalezgranados/.m2/repository/org/bouncycastle/bc-fips/1.0.2.1/bc-fips-1.0.2.1.jar -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
```