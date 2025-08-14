package io.quarkus.ts.http.grpc.customizers;

import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
record InterceptorInvocations(String interceptedFirst, String interceptedSecond, String interceptedThird) {

    InterceptorInvocations() {
        this(null, null, null);
    }

    InterceptorInvocations withInvocation(String customizerName) {
        Objects.requireNonNull(customizerName);
        if (interceptedFirst == null) {
            return new InterceptorInvocations(customizerName, null, null);
        }
        if (interceptedSecond == null) {
            return new InterceptorInvocations(interceptedFirst, customizerName, null);
        }
        if (interceptedThird == null) {
            return new InterceptorInvocations(interceptedFirst, interceptedSecond, customizerName);
        }
        throw new IllegalStateException();
    }
}
