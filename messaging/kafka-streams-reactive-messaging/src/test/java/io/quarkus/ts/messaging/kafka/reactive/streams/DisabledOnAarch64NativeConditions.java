package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.quarkus.test.services.quarkus.model.QuarkusProperties;

public class DisabledOnAarch64NativeConditions implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        boolean isNative = QuarkusProperties.isNativeEnabled();
        boolean isAarch64 = "true".equals(System.getProperty("ts.arm.missing.services.excludes"));
        if (isAarch64 && isNative) {
            return ConditionEvaluationResult.disabled("It is running on aarch64 in native mode.");
        } else {
            return ConditionEvaluationResult.enabled("It is not running on aarch64 in native mode.");
        }
    }
}
