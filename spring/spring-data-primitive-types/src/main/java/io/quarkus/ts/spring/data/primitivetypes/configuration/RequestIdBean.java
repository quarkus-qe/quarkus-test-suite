package io.quarkus.ts.spring.data.primitivetypes.configuration;

public class RequestIdBean {

    private final String requestId;

    public RequestIdBean(String requestId, String instanceId) {
        this.requestId = String.format("%s_%s", instanceId, requestId);
    }

    public String getRequestId() {
        return requestId;
    }

}
