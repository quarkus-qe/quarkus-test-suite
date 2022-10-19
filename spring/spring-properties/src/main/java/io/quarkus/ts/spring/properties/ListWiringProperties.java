package io.quarkus.ts.spring.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lists")
public class ListWiringProperties {
    public List<String> strings; // Cover injection of lists of strings;
}
