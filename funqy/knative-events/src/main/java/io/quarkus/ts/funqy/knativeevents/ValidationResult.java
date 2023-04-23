package io.quarkus.ts.funqy.knativeevents;

import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PENG_EXPECTED_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PONG_EXPECTED_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PUNG_EXPECTED_VALUE;

public class ValidationResult {

    private Functions invokedFunction;
    private Object actualValue;

    public ValidationResult(Functions invokedFunction, Object actualValue) {
        this.invokedFunction = invokedFunction;
        this.actualValue = actualValue;
    }

    public Functions getInvokedFunction() {
        return invokedFunction;
    }

    public void setInvokedFunction(Functions invokedFunction) {
        this.invokedFunction = invokedFunction;
    }

    public Object getActualValue() {
        return actualValue;
    }

    public void setActualValue(Object actualValue) {
        this.actualValue = actualValue;
    }

    public enum Functions {
        FALLBACK(true),
        PING(true),
        PUNG(PUNG_EXPECTED_VALUE),
        PENG(PENG_EXPECTED_VALUE),
        PONG(PONG_EXPECTED_VALUE),
        ENV_VAR(ENV_VAR_VALUE);

        public final Object expectedValue;

        Functions(Object expectedValue) {
            this.expectedValue = expectedValue;
        }
    }
}
