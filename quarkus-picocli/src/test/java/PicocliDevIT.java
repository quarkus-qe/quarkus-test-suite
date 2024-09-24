import static java.util.concurrent.CompletableFuture.runAsync;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.qe.command.AgeCommand;
import io.quarkus.ts.qe.command.EntryCommand;
import io.quarkus.ts.qe.command.HelloCommand;
import io.quarkus.ts.qe.configuration.Config;
import io.quarkus.ts.qe.services.AgeService;
import io.quarkus.ts.qe.services.HelloService;

@DisabledOnNative(reason = "supporting only for JVM")
@QuarkusScenario
public class PicocliDevIT {

    @QuarkusApplication(classes = { EntryCommand.class, Config.class, HelloCommand.class, AgeCommand.class, AgeService.class,
            HelloService.class }, properties = "dev.properties")
    static final RestService greetingApp = new RestService()
            .withProperty("quarkus.args", "greeting --name QE")
            .setAutoStart(false);

    @QuarkusApplication(classes = { EntryCommand.class, Config.class, HelloCommand.class, AgeCommand.class, AgeService.class,
            HelloService.class }, properties = "dev.properties")
    static final RestService ageApp = new RestService()
            .withProperty("quarkus.args", "age --age 30")
            .setAutoStart(false);

    @QuarkusApplication(classes = { EntryCommand.class, Config.class, HelloCommand.class, AgeCommand.class, AgeService.class,
            HelloService.class }, properties = "dev.properties")
    static final RestService greetingBlankArgumentApp = new RestService()
            .withProperty("quarkus.args", " --name QE")
            .setAutoStart(false);

    @QuarkusApplication(classes = { EntryCommand.class, Config.class, HelloCommand.class, AgeCommand.class, AgeService.class,
            HelloService.class }, properties = "dev.properties")
    static final RestService greetingInvalidArgumentApp = new RestService()
            .withProperty("quarkus.args", "greeting -x QE")
            .setAutoStart(false);

    @QuarkusApplication(classes = { EntryCommand.class, Config.class, HelloCommand.class, AgeCommand.class, AgeService.class,
            HelloService.class }, properties = "dev.properties")
    static final RestService bothTopCommandApp = new RestService()
            .setAutoStart(false);

    @Test
    public void verifyErrorForApplicationScopedBeanInPicocliCommand() {
        try {
            runAsync(ageApp::start);
            ageApp.logs().assertContains("CDI: programmatic lookup problem detected");
        } finally {
            ageApp.stop();
        }
    }

    @Test
    public void verifyGreetingCommandOutputsExpectedMessage() {
        try {
            runAsync(greetingApp::start);
            greetingApp.logs().assertContains("Hello QE!");
        } finally {
            greetingApp.stop();
        }
    }

    @Test
    void verifyErrorForBlankArgumentsInGreetingCommand() {
        try {
            runAsync(greetingBlankArgumentApp::start);
            greetingBlankArgumentApp.logs().assertContains("Unmatched arguments from index 0: '', '--name', 'QE'");
        } finally {
            greetingBlankArgumentApp.stop();
        }
    }

    @Test
    void verifyErrorForInvalidArgumentsInGreetingCommand() {
        try {
            runAsync(greetingInvalidArgumentApp::start);
            greetingInvalidArgumentApp.logs().assertContains("Unknown options: '-x', 'QE'");
        } finally {
            greetingInvalidArgumentApp.stop();
        }
    }

    /**
     * Chain Commands in a Single Execution is not possible
     */
    @Test
    public void verifyErrorForMultipleCommandsWithoutTopCommand() {
        bothTopCommandApp
                .withProperty("quarkus.args", "greeting --name EEUU age --age 247");
        try {
            runAsync(bothTopCommandApp::start);
            bothTopCommandApp.logs().assertContains("Unmatched arguments from index 3: 'age', '--age', '247'");
        } finally {
            bothTopCommandApp.stop();
        }
    }

}
