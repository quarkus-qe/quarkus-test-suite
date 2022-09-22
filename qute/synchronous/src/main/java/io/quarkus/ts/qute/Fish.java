package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateData;

@TemplateData
public class Fish {
    private final String name;

    public Fish(String name) {
        this.name = name;
    }

    public String saySomething() {
        return String.format("This %s stays silent", this.name);
    }

    public String slap(String user, String target) {
        return String.format("%s slaps %s around a bit with a large %s", user, target, this.name);
    }
}
