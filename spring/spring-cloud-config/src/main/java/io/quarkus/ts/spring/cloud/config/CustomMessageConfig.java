package io.quarkus.ts.spring.cloud.config;

// TODO Disabled because https://github.com/quarkusio/quarkus/issues/19448
// @ConfigMapping(prefix = "custom")
public interface CustomMessageConfig {

    String message();
}
