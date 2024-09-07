package io.quarkus.ts.http.grpc;

import java.util.List;

public final class GrpcReflectionResponse {
    private int serviceCount;
    private List<String> serviceList;

    public GrpcReflectionResponse() {
    }

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

    public void setServiceCount(int serviceCount) {
        this.serviceCount = serviceCount;
    }

    public void setServiceList(List<String> serviceList) {
        this.serviceList = serviceList;
    }
}
