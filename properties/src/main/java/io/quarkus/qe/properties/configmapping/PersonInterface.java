package io.quarkus.qe.properties.configmapping;

import java.util.Map;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "person")
public interface PersonInterface {
    String name();

    int age();

    Map<String, String> labels();
}
