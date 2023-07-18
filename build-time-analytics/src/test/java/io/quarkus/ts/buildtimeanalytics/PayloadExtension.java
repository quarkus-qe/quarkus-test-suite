package io.quarkus.ts.buildtimeanalytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayloadExtension {
    @JsonProperty("version")
    private String version;

    @JsonProperty("group_id")
    private String groupId;

    @JsonProperty("artifact_id")
    private String artifactId;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }
}
