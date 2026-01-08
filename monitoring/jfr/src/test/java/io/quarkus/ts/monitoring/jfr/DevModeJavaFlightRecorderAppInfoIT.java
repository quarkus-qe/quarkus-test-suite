package io.quarkus.ts.monitoring.jfr;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-6552")
@QuarkusScenario
@DisabledOnSemeruJdk(reason = "Semeru don't have full support for JFR yet")
public class DevModeJavaFlightRecorderAppInfoIT extends AbstractJavaFlightRecorderAppInfoIT {

    @DevModeQuarkusApplication
    static final RestService app = new RestService()
            .withProperty("jvm.args", "-XX:StartFlightRecording=dumponexit=true,filename=" + RECORDING_PATH)
            .setAutoStart(false);
}
