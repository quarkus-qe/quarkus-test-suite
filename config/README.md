Secrets generation:

- Base64 secret was generated with `echo 'quarkus-qe-base64' | base64` command
- SHA256 secret was generated with `echo -n "quarkus-qe-sha256" | openssl dgst -sha256` command
- AES/GCM/NO-PADDING was generated with this method https://github.com/quarkiverse/quarkus-file-vault/blob/main/file-vault-common/src/main/java/io/quarkiverse/filevault/util/EncryptionUtil.java#L52

KeyStores generation:

- `plain-keystore` was generated with `keytool -importpass -alias plain-keystore-config-key -keystore plain-keystore -storepass quarkuspwd -storetype PKCS12 -v` and prompt input `plain-keystore-config-value`
- `base64-keystore` was generated with `keytool -importpass -alias secrets.keystore-base64-handler -keystore base64-keystore -storepass quarkuspwd -storetype PKCS12 -v` and prompt input `cXVhcmt1cy1xZS1iYXNlNjQ=`
- `sha256-keystore` was generated with `keytool -importpass -alias secrets.keystore-sha256-handler -keystore sha256-keystore -storepass quarkuspwd -storetype PKCS12 -v` and prompt input `0e8c2a8b8ecfbd52c3ef17acd44498ee2b892c66b308598f6a88ca8c7a235c4e`
- `aes-gcm-nopadding-keystore` was generated with `keytool -importpass -alias secrets.keystore-crypto-handler -keystore aes-gcm-nopadding-keystore -storepass quarkuspwd -storetype PKCS12 -v` and prompt input `DEaZok2mA76F-jak70kWav7Gx65QarcWbul-bvLgCHzy9eiHMkCWdadFISES2H7lewF61Ct-jqNaSQ`
