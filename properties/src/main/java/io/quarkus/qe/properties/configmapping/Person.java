package io.quarkus.qe.properties.configmapping;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "person")
public class Person {
    String name;
    int age;
}
