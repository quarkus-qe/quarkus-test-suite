package io.quarkus.ts.external.applications;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.quarkus.test.services.quarkus.model.QuarkusProperties;

public class DisabledOnAarch64SnapshotConditions implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        boolean isAarch64 = "true".equals(System.getProperty("ts.arm.missing.services.excludes"));
        boolean isSnapshot = QuarkusProperties.getVersion().contains("SNAPSHOT");
        if (isAarch64 && isSnapshot) {
            return ConditionEvaluationResult.disabled("It is snapshot running on aarch64.");
        } else {
            return ConditionEvaluationResult.enabled("It is not snapshot running on aarch64.");
        }
    }
}
