package io.quarkus.ts.http.minimum.reactive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cluster {
    private static final String TEAM_ID = "qe";
    private static final String PROVIDER_ID = "quarkus.io";
    private static final String REGION_ID = "EU";
    private static final String NAME = "QE";
    private static final String NETWORK_ID = "qe";

    @JsonProperty("team_id")
    public String teamId;
    @JsonProperty("provider_id")
    public String providerId;
    @JsonProperty("region_id")
    public String regionId;
    @JsonProperty("name")
    public String name;
    @JsonProperty("network_id")
    public String networkId;
    @JsonProperty("major_version")
    public Integer majorVersion;
    @JsonProperty("storage")
    public Integer storage;
    public Integer cpu;
    public Integer memory;

    public static Cluster createDefault() {
        return new Cluster(TEAM_ID, PROVIDER_ID, REGION_ID, NAME, NETWORK_ID);
    }

    public Cluster(String teamId, String providerId, String regionId, String name, String networkId) {
        this.teamId = teamId;
        this.providerId = providerId;
        this.regionId = regionId;
        this.name = name;
        this.networkId = networkId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Integer getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(Integer majorVersion) {
        this.majorVersion = majorVersion;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }
}
