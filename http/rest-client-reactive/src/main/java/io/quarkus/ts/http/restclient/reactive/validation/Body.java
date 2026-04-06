package io.quarkus.ts.http.restclient.reactive.validation;

public class Body {
    private String prop;

    public Body(String prop) {
        this.prop = prop;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }
}
