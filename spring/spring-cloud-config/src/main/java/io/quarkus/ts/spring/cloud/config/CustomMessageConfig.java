package io.quarkus.ts.spring.cloud.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "custom")
public interface CustomMessageConfig {

    String message();
}
