package io.quarkus.ts.monitoring.opentracing.reactive.grpc;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
@Disabled("RESTEasy Reactive + SSE + OpenTracing are not friends, works with OpenTelemetry," +
        "https://github.com/quarkusio/quarkus/issues/29539 closed as won't fix")
public class ServerSentEventsTraceOpentracingIT extends AbstractTraceIT {

    @Override
    protected String endpointPrefix() {
        return "server-sent-events";
    }
}
