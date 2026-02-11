package io.quarkus.ts.sqldb.panacheflyway.init.yaml;

import java.util.ArrayList;
import java.util.Map;

public class CustomYaml {
    private final Map content;

    public CustomYaml(Object content) {
        this.content = (Map) content;
    }

    public CustomYaml get(String name) {
        return new CustomYaml(this.content.get(name));
    }

    public String getValue(String name) {
        return (String) this.content.get(name);
    }

    public CustomYaml getFromArray(String name, int i) {
        return new CustomYaml(((ArrayList) this.content.get(name)).get(i));
    }
}
