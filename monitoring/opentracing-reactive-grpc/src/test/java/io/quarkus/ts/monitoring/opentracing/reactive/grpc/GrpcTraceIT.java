package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@Disabled("Caused by https://github.com/quarkusio/quarkus/issues/13224")
@QuarkusScenario
@DisabledOnNative
public class GrpcTraceIT extends AbstractTraceIT {

    @Override
    protected String endpointPrefix() {
        return "grpc";
    }

}
