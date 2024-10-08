package io.quarkus.ts.http.advanced.reactive.brotli4j;

import jakarta.enterprise.context.ApplicationScoped;

import io.netty.handler.codec.compression.BrotliOptions;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.quarkus.vertx.http.HttpServerOptionsCustomizer;
import io.vertx.core.http.HttpServerOptions;

@ApplicationScoped
public class Brotli4JHttpServerConfig implements HttpServerOptionsCustomizer {
    // It depends on compression level that we want to apply, in this case we use level 4, but we won't test the level compression features.
    private static final int compressionLevel = 4;

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
