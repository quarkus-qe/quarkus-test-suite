package io.quarkus.qe.hibernate;

import java.util.Map;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.DevModeQuarkusService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.smallrye.common.os.OS;

@QuarkusScenario
@Tag("QUARKUS-6243")
public class DevModePostgresqlHQLConsoleIT extends AbstractHQLConsoleIT {

    @DevModeQuarkusApplication
    static DevModeQuarkusService app = (DevModeQuarkusService) new DevModeQuarkusService()
            .withProperties(() -> {
                if (OS.WINDOWS.isCurrent() && DockerUtils.isNotPodman()) {
                    String dockerIp = System.getenv(DockerUtils.DOCKER_IP);
                    if (dockerIp != null && !dockerIp.isEmpty()) {
                        // container IP in Minikube is not localhost
                        return Map.of("quarkus.datasource.dev-ui.allowed-db-host", dockerIp);
                    }
                }
                return Map.of();
            });
}
