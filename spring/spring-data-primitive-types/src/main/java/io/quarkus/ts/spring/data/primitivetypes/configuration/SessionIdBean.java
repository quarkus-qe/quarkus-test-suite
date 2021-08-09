package io.quarkus.ts.spring.data.primitivetypes.configuration;

public class SessionIdBean {

    private final String sessionId;

    public SessionIdBean(String sessionId, String instanceId) {
        this.sessionId = String.format("%s_%s", instanceId, sessionId);
    }

    public String getSessionId() {
        return sessionId;
    }
}
