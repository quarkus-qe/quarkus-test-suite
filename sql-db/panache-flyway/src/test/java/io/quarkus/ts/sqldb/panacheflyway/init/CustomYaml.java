package io.quarkus.ts.sqldb.panacheflyway.init;

import java.util.ArrayList;
import java.util.Map;

class CustomYaml {
    private final Map content;

    CustomYaml(Object content) {
        this.content = (Map) content;
    }

    CustomYaml get(String name) {
        return new CustomYaml(this.content.get(name));
    }

    String getValue(String name) {
        return (String) this.content.get(name);
    }

    public CustomYaml getFromArray(String name, int i) {
        return new CustomYaml(((ArrayList) this.content.get(name)).get(i));
    }
}
