import static java.util.concurrent.CompletableFuture.runAsync;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.qe.command.CommonOptions;
import io.quarkus.ts.qe.command.OtherCommand;
import io.quarkus.ts.qe.command.OtherEntryCommand;
import io.quarkus.ts.qe.configuration.Config;

@DisabledOnNative(reason = "supporting only for JVM")
@QuarkusScenario
public class PicocliProdIT {
    @QuarkusApplication(classes = { OtherEntryCommand.class, Config.class, OtherCommand.class,
            CommonOptions.class }, properties = "prod.properties")
    static final RestService customized = new RestService()
            .withProperty("quarkus.args", "start -t 60 -v")
            .setAutoStart(false);

    @Test
    public void verifyCustomizedCommandLineBehavior() {
        String expectedOutput = "Service started with timeout: 60 and verbosity";
        try {
            runAsync(customized::start);
            customized.logs().assertContains(expectedOutput);
        } finally {
            customized.stop();
        }
    }
}
