package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ReactiveTraceOpentracingIT extends AbstractTraceIT {

    @Override
    protected String endpointPrefix() {
        return "reactive";
    }
}
