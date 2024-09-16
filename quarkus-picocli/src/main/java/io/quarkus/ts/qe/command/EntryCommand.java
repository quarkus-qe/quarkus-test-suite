package io.quarkus.ts.qe.command;

import io.quarkus.logging.Log;

import picocli.CommandLine;

@CommandLine.Command(name = "customized-command", mixinStandardHelpOptions = true, subcommands = { HelloCommand.class,
        AgeCommand.class })
public class EntryCommand implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        Log.info("Running EntryCommand with name: " + spec.name());
    }
}
