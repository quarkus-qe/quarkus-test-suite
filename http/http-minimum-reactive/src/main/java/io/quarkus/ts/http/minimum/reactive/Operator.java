
package io.quarkus.ts.http.minimum.reactive;

public class Operator {
    private String name;

    public Operator() {
        this.name = null;
    }

    public Operator(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
