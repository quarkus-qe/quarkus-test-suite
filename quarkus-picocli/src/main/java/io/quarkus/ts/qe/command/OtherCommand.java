package io.quarkus.ts.qe.command;

import io.quarkus.logging.Log;

import picocli.CommandLine;

@CommandLine.Command(name = "start", description = "Starts the service.")
public class OtherCommand implements Runnable {

    @CommandLine.Mixin
    CommonOptions commonOptions;

    @CommandLine.Option(names = { "-t", "--timeout" }, description = "Timeout in seconds", defaultValue = "60")
    int timeout;

    @Override
    public void run() {
        Log.infof("Service started with timeout: %d and verbosity: %s%n", timeout, commonOptions);
    }

}
