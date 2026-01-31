package io.quarkus.ts.monitoring.jfr;

import static io.quarkus.ts.monitoring.jfr.JfrUtils.RECORDING_PATH;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6552")
@QuarkusScenario
@DisabledOnSemeruJdk(reason = "Semeru don't have full support for JFR yet")
public class JavaFlightRecorderAppInfoIT extends AbstractJavaFlightRecorderAppInfoIT {

    @QuarkusApplication
    static final RestService app = new RestService()
            .onPreStop(JfrUtils::dumpJfrRecording)
            .withProperty("-XX", "StartFlightRecording=dumponexit=true,filename=" + RECORDING_PATH)
            .setAutoStart(false);
}
