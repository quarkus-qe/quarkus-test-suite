package io.quarkus.ts.quarkus.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.QuarkusCliClient;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;

@Tag("QUARKUS-960")
@Tag("quarkus-cli")
@QuarkusScenario
@DisabledOnQuarkusVersion(version = "1\\..*", reason = "Quarkus CLI has been reworked in 2.x")
public class QuarkusCliHelpIT {

    static final String HELP_COMMAND = "--help";

    @Inject
    static QuarkusCliClient cliClient;

    @Test
    public void shouldCommandsHaveHelpOption() {
        for (CommandWithHelp command : CommandWithHelp.values()) {
            QuarkusCliClient.Result result = cliClient.run(command.getCommand(), HELP_COMMAND);
            assertTrue(result.isSuccessful(), "Help command for '" + command.getCommand() + "' didn't work");
            assertTrue(result.getOutput().contains(command.getExpectedHelp()),
                    "Unexpected help output for Command '" + command.getCommand() + "': " + result.getOutput());
        }
    }

    enum CommandWithHelp {
        CREATE("create", "Create a new project."),
        CREATE_CLI("create cli", "Create a Quarkus command-line project."),
        CREATE_APP("create app", "Create a Quarkus application project."),
        BUILD("build", "Build the current project."),
        DEV("dev", "Run the current project in dev (live coding) mode."),
        EXTENSION("extension", "List, add, and remove extensions of an existing project."),
        COMPLETION("completion", "bash/zsh completion:  source <(quarkus completion)"),
        VERSION("version", "Display version information");

        private final String command;
        private final String expectedHelp;

        CommandWithHelp(String command, String expectedHelp) {
            this.command = command;
            this.expectedHelp = expectedHelp;
        }

        public String getCommand() {
            return command;
        }

        public String getExpectedHelp() {
            return expectedHelp;
        }
    }
}
