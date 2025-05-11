package hero;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.logging.Log;
import io.quarkus.tls.runtime.KeyStoreAndKeyCertOptions;
import io.quarkus.tls.runtime.KeyStoreProvider;
import io.smallrye.common.annotation.Identifier;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.PemKeyCertOptions;

@ApplicationScoped
@Identifier("tls-certificate-client")
public class InMemoryClientKeyStoreProvider implements KeyStoreProvider {

    private volatile String tlsKey;
    private volatile String tlsCert;

    @Override
    public KeyStoreAndKeyCertOptions getKeyStore(Vertx vertx) {
        try {
            var options = new PemKeyCertOptions()
                    .addCertValue(Buffer.buffer(getTlsCert()))
                    .addKeyValue(Buffer.buffer(getTlsKey()));
            var keyStore = options.loadKeyStore(vertx);
            return new KeyStoreAndKeyCertOptions(keyStore, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTlsKey() {
        if (tlsKey == null) {
            synchronized (this) {
                if (tlsKey == null) {
                    tlsKey = loadCert("/etc/tls/tls.key");
                }
            }
        }
        return tlsKey;
    }

    private String getTlsCert() {
        if (tlsCert == null) {
            synchronized (this) {
                if (tlsCert == null) {
                    tlsCert = loadCert("/etc/tls/tls.crt");
                }
            }
        }
        return tlsCert;
    }

    private static String loadCert(String certificatePath) {
        try {
            var certificateContent = Files.readString(Path.of(certificatePath));
            Log.info("Loaded certificate from " + certificatePath);
            return certificateContent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void updateKeyStore(String tlsKey, String tlsCert) {
        this.tlsKey = tlsKey;
        this.tlsCert = tlsCert;
    }
}
