package io.quarkus.qe;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class ReactivePingPongResourceOpentracingIT extends AbstractPingPongResourceIT {

    @Override
    protected String endpointPrefix() {
        return "reactive";
    }
}
