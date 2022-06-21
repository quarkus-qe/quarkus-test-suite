package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class RestTraceOpentracingIT extends AbstractTraceIT {

    @Override
    protected String endpointPrefix() {
        return "rest";
    }
}
