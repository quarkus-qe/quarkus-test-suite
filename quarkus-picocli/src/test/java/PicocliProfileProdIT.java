import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.qe.command.AgeCommand;
import io.quarkus.ts.qe.command.CommonOptions;
import io.quarkus.ts.qe.command.EntryCommand;
import io.quarkus.ts.qe.command.HelloCommand;
import io.quarkus.ts.qe.command.OtherCommand;
import io.quarkus.ts.qe.command.OtherEntryCommand;
import io.quarkus.ts.qe.configuration.Configuration;
import io.quarkus.ts.qe.services.AgeService;
import io.quarkus.ts.qe.services.HelloService;

@QuarkusScenario
public class PicocliProfileProdIT {

    @QuarkusApplication(classes = { AgeCommand.class, CommonOptions.class, EntryCommand.class, HelloCommand.class,
            OtherCommand.class, OtherEntryCommand.class,
            Configuration.class, AgeService.class, HelloService.class }, properties = "prod.properties")
    static final RestService customized = new RestService()
            .withProperty("quarkus.args", "start -t 60 -v");

    @Test
    public void verifyCustomizedCommandLineBehavior() {
        String expectedOutput = "Service started with timeout: 60 and verbosity";
        customized.logs().assertContains(expectedOutput);
    }
}
