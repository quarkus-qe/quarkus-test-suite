package io.quarkus.ts.openshift.security.basic;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class SecurityTestProfile implements QuarkusTestProfile {
    public static final String PROFILE = "security-test";

    @Override
    public String getConfigProfile() {
        return PROFILE;
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.security.jaxrs.deny-unannotated-endpoints", "true");
    }
}
