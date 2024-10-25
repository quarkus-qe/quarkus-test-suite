package io.quarkus.ts.annotations;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.quarkus.test.scenarios.annotations.CheckIfSystemPropertyCondition;

public class EnabledIfPostgresImageCommunityCondition extends CheckIfSystemPropertyCondition {

    @Override
    protected String getSystemPropertyName(ExtensionContext context) {
        return "postgresql.latest.image";
    }

    @Override
    protected boolean checkEnableCondition(ExtensionContext context, String actual) {
        return actual != null && actual.contains("docker.io");

    }
}
