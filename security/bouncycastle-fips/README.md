# BouncyCastle FIPS / JSSE

## Generate certificates steps (Keytool manual process)

Generate a self-signed PEM
```shell
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem
```

Convert the certificate from PEM to PKCS12, using the following command:
```shell
openssl pkcs12 -export -out Cert.p12 -in cert.pem -inkey key.pem
```

Generate truststore from p12 file
```shell
keytool -importkeystore -srckeystore Cert.p12 -srcstoretype pkcs12 -destkeystore server-truststore.jks -deststoretype BCFKS -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $PATH_TO_BC_FIPS_JAR -storetype BCFKS -validity 3000
```

Generate keystore
```shell
keytool -genkey -alias server -keyalg RSA -keystore server-keystore.jks -keysize 2048 -keypass password -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $PATH_TO_BC_FIPS_JAR -storetype BCFKS -validity 3000
```
