package io.quarkus.ts.http.advanced.reactive.clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

public class RestClientServiceBuilder<T> {

    protected String uri;
    protected String password;
    protected boolean hostVerified;
    protected String keyStorePath;

    public RestClientServiceBuilder(String uri) {
        this.uri = uri;
    }

    public RestClientServiceBuilder<T> withPassword(String password) {
        this.password = password;
        return this;
    }

    public RestClientServiceBuilder<T> withHostVerified(Boolean verified) {
        this.hostVerified = verified;
        return this;
    }

    public RestClientServiceBuilder<T> withKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
        return this;
    }

    public T build(Class<T> clazz) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(URI.create(this.uri));
        builder.hostnameVerifier((s, sslSession) -> this.hostVerified);
        if (!StringUtils.isEmpty(this.keyStorePath)) {
            builder.trustStore(loadKeystore());
        }

        return builder.build(clazz);
    }

    protected KeyStore loadKeystore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ClassLoader classLoader = getClass().getClassLoader();
        File keyStore = new File(classLoader.getResource(this.keyStorePath).getFile());
        try (FileInputStream fis = new FileInputStream(keyStore)) {
            ks.load(fis, this.password.toCharArray());
        }
        return ks;
    }
}
