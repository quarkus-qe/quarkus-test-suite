package io.quarkus.ts.stork.custom;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "custom.external.service")
public interface InMemoryConfiguration {
    String port();
    String host();
}
