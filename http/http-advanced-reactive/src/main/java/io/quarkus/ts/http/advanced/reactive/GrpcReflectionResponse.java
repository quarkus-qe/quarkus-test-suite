package io.quarkus.ts.http.advanced.reactive;

import java.util.List;

public final class GrpcReflectionResponse {
    private final int serviceCount;
    private final List<String> serviceList;

    public GrpcReflectionResponse(int serviceCount, List<String> serviceList) {
        this.serviceCount = serviceCount;
        this.serviceList = serviceList;
    }

    public List<String> getServiceList() {
        return this.serviceList;
    }

    public int getServiceCount() {
        return serviceCount;
    }

}
