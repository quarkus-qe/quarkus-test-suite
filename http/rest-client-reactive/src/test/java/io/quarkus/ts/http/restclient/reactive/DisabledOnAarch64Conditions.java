package io.quarkus.ts.http.restclient.reactive;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DisabledOnAarch64Conditions implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        boolean isAarch64 = "true".equals(System.getProperty("ts.arm.missing.services.excludes"));
        if (isAarch64) {
            return ConditionEvaluationResult.disabled("It is running on aarch64");
        } else {
            return ConditionEvaluationResult.enabled("It is not running on aarch64");
        }
    }
}