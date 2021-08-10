package io.quarkus.ts.messaging.kafka.aggregator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class LoginAggregation {
    private String endpoint;
    private int code;
    private int count;

    public LoginAggregation updateFrom(LoginAttempt loginAttempt) {
        endpoint = loginAttempt.getEndpoint();
        code = loginAttempt.getCode();
        count++;

        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
