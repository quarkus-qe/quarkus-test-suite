package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.quarkus.test.services.quarkus.model.QuarkusProperties;

public class DisabledOnRHBQAarch64NativeConditions implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        boolean isRHBQ = QuarkusProperties.getVersion().contains("redhat");
        boolean isNative = QuarkusProperties.isNativePackageType();
        boolean isAarch64 = "true".equals(System.getProperty("ts.arm.missing.services.excludes"));
        if (isRHBQ && isAarch64 && isNative) {
            return ConditionEvaluationResult.disabled("It is RHBQ running on aarch64 in native mode.");
        } else {
            return ConditionEvaluationResult.enabled("It is not RHBQ running on aarch64 in native mode.");
        }
    }
}
