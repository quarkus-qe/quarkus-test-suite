package io.quarkus.qe;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class RestPingPongResourceOpentracingIT extends AbstractPingPongResourceIT {

    @Override
    protected String endpointPrefix() {
        return "rest";
    }
}
