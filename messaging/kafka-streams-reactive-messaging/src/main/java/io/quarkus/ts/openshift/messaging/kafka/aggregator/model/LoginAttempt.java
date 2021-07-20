package io.quarkus.ts.openshift.messaging.kafka.aggregator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class LoginAttempt {
    private String id;
    private String endpoint;
    private int code;

    public LoginAttempt(String id, String endpoint, int code) {
        this.id = id;
        this.endpoint = endpoint;
        this.code = code;
    }

    public LoginAttempt() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
