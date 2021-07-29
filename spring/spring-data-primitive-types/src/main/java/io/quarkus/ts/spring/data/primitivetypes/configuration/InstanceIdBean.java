package io.quarkus.ts.spring.data.primitivetypes.configuration;

public class InstanceIdBean {

    private final String instanceId;

    protected InstanceIdBean(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
