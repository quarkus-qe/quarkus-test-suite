package io.quarkus.ts.security.https;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Unremovable;
import io.quarkus.tls.KeyStoreAndKeyCertOptions;
import io.quarkus.tls.KeyStoreFactory;
import io.quarkus.tls.OtherKeyStoreConfiguration;
import io.smallrye.common.annotation.Identifier;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyStoreOptions;

@ApplicationScoped
@Unremovable
@Identifier("custom-keystore")
public class CustomKeyStoreFactory implements KeyStoreFactory {
    @Override
    public KeyStoreAndKeyCertOptions createKeyStore(OtherKeyStoreConfiguration config, Vertx vertx, String name) {
        String format = config.params().get("key-store-format");
        Path path = Path.of(config.params().get("key-store-path"));
        String password = config.password()
                .orElseThrow(() -> new IllegalStateException("No password configured for the '" + name + "' key store"));

        try {
            byte[] content = Files.readAllBytes(path);
            KeyStore keyStore = KeyStore.getInstance(format);
            keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
            KeyStoreOptions options = new KeyStoreOptions()
                    .setType(format)
                    .setPassword(password)
                    .setValue(Buffer.buffer(content));
            return new KeyStoreAndKeyCertOptions(keyStore, options);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the '" + format + "' key store from " + path, e);
        }
    }
}
