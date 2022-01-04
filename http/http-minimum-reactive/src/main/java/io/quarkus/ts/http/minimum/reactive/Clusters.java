package io.quarkus.ts.http.minimum.reactive;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Clusters {
    @JsonProperty("clusters")
    List<Cluster> clusterList;

    public Clusters() {
    }

    public Clusters(Cluster cluster) {
        clusterList = List.of(cluster);
    }

    public Clusters(List<Cluster> clusterList) {
        this.clusterList = clusterList;
    }

    public List<Cluster> getClusterList() {
        return clusterList;
    }

    public void setClusterList(List<Cluster> clusterList) {
        this.clusterList = clusterList;
    }
}
