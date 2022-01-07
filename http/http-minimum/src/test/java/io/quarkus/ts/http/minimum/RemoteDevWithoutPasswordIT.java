package io.quarkus.ts.http.minimum;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.RemoteDevModeQuarkusApplication;

@QuarkusScenario
public class RemoteDevWithoutPasswordIT {

    @RemoteDevModeQuarkusApplication(password = "")
    static RestService app = new DevModeQuarkusService().setAutoStart(false);

    @Tag("QUARKUS-527")
    @Test
    public void remoteQuarkusDevExecutionWithoutPasswordShouldFail() {
        assertThrows(AssertionError.class, () -> app.start(), "Should fails because live reload URL set without password");
        app.logs().assertContains("Live reload URL set but no password, remote dev requires a password");
    }
}
