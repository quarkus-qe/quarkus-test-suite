File `keystore.jks` was generated with command:

```bash
keytool -genkey -alias mykeystore \
    -keyalg RSA -keystore keystore.jks \
    -dname "CN=John Doe, OU=RedHat, O=RedHat, L=Ostrava, S=Czechia, C=CZ" \
    -storepass password -keypass password
```