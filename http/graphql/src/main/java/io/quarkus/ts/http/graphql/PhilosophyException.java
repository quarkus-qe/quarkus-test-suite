package io.quarkus.ts.http.graphql;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("42")
public class PhilosophyException extends RuntimeException {
}
