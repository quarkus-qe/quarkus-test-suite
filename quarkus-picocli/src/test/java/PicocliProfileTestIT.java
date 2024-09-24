import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
public class PicocliProfileTestIT {

    @QuarkusApplication(classes = { AgeCommand.class, CommonOptions.class, EntryCommand.class, HelloCommand.class,
            OtherCommand.class, OtherEntryCommand.class,
            Configuration.class, AgeService.class, HelloService.class }, properties = "test.properties")
    static final RestService app = new RestService().withProperty("quarkus.args", "age --age 30");

    @Order(1)
    @Test
    public void verifyErrorForApplicationScopedBeanInPicocliCommand() {
        app.logs().assertContains("CDI: programmatic lookup problem detected");
    }

    @Order(2)
    @Test
    public void verifyGreetingCommandOutputsExpectedMessage() {
        app.withProperty("quarkus.args", "greeting --name QE");
        app.restart();
        app.logs().assertContains("Hello QE!");
    }

    @Order(3)
    @Test
    void verifyErrorForBlankArgumentsInGreetingCommand() {
        app.withProperty("quarkus.args", " --name QE");
        app.restart();
        app.logs().assertContains("Unmatched arguments from index 0: '', '--name', 'QE'");
    }

    @Order(4)
    @Test
    void verifyErrorForInvalidArgumentsInGreetingCommand() {
        app.withProperty("quarkus.args", "greeting -x QE");
        app.restart();
        app.logs().assertContains("Unknown options: '-x', 'QE'");
    }

    /**
     * Chain Commands in a Single Execution is not possible
     */
    @Order(5)
    @Test
    public void verifyErrorForMultipleCommandsWithoutTopCommand() {
        app.withProperty("quarkus.args", "greeting --name EEUU age --age 247");
        app.restart();
        app.logs().assertContains("Unmatched arguments from index 3: 'age', '--age', '247'");
    }

}
