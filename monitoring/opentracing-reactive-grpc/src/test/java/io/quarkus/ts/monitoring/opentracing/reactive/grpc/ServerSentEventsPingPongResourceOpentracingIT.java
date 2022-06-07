package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ServerSentEventsPingPongResourceOpentracingIT extends AbstractPingPongResourceIT {

    @Override
    protected String endpointPrefix() {
        return "server-sent-events";
    }
}
