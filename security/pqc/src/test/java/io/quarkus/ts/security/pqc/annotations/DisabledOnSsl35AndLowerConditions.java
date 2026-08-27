package io.quarkus.ts.security.pqc.annotations;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.netty.handler.ssl.OpenSsl;

public class DisabledOnSsl35AndLowerConditions implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        boolean isOpenSslNewerThen35 = OpenSsl.isAvailable() && OpenSsl.version() >= 0x30500000L;
        if (isOpenSslNewerThen35) {
            return ConditionEvaluationResult.enabled("OpenSsl newer then 3.5 is available");
        } else {
            return ConditionEvaluationResult.disabled("OpenSsl newer then 3.5 is not available");
        }
    }
}
