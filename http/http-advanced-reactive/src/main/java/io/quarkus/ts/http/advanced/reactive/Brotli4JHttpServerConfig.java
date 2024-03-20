package io.quarkus.ts.http.advanced.reactive;

import jakarta.enterprise.context.ApplicationScoped;

import io.netty.handler.codec.compression.BrotliOptions;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;

@Startup
@ApplicationScoped
public class Brotli4JHttpServerConfig implements HttpServerOptionsCustomizer {
    // It depends on compression level that we want apply
    private int compressionLevel = 4;

    @Override
    public void customizeHttpServer(HttpServerOptions options) {
        options.addCompressor(getBrotliOptions(compressionLevel));
    }

    @Override
    public void customizeHttpsServer(HttpServerOptions options) {
        options.addCompressor(getBrotliOptions(compressionLevel));
    }

    private static BrotliOptions getBrotliOptions(int compressionLevel) {
        return StandardCompressionOptions.brotli();
    }
}