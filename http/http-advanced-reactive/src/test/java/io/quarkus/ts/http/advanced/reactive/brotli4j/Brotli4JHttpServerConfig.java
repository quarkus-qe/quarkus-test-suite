package io.quarkus.ts.http.advanced.reactive.brotli4j;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.vertx.http.HttpServerConfigCustomizer;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.net.ServerSSLOptions;

@ApplicationScoped
public class Brotli4JHttpServerConfig implements HttpServerConfigCustomizer {

    @Override
    public void customizeHttpServer(HttpServerConfig config) {
        config.getCompressionConfig().addBrotli();
    }

    @Override
    public void customizeHttpsServer(HttpServerConfig config, ServerSSLOptions sslOptions) {
        config.getCompressionConfig().addBrotli();
    }
}
