package io.quarkus.ts.jaxrs.reactive.client;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PojoData {
    public String foo;
    public Integer bar;
}
