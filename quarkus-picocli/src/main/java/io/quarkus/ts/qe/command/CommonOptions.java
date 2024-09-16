package io.quarkus.ts.qe.command;

import picocli.CommandLine;

public class CommonOptions {
    @CommandLine.Option(names = { "-v", "--verbose" }, description = "Enable verbose mode")
    boolean verbose;
}
