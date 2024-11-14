package io.quarkus.ts.many.extensions;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Hello(String content) {

}
