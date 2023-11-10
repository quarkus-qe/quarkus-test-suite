package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
@Disabled("Hitting RESTEasy Classic limits with SSE, details https://github.com/quarkusio/quarkus/issues/36979")
public class ServerSentEventsTraceOpentracingIT extends AbstractTraceIT {

    @Override
    protected String endpointPrefix() {
        return "server-sent-events";
    }
}
