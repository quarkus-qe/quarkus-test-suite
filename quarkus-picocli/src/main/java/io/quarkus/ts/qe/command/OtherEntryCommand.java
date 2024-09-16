package io.quarkus.ts.qe.command;

import io.quarkus.logging.Log;

import picocli.CommandLine;

@CommandLine.Command(name = "other", mixinStandardHelpOptions = true, subcommands = { OtherCommand.class })
public class OtherEntryCommand implements Runnable {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        Log.info("OtherEntryCommand with name " + spec.name());
    }
}
