package io.quarkus.ts.qe.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.ts.qe.command.EntryCommand;
import io.quarkus.ts.qe.command.OtherEntryCommand;

@ApplicationScoped
public class Configuration {

    @Produces
    @TopCommand
    @IfBuildProfile("test")
    public Class<EntryCommand> devCommand() {
        return EntryCommand.class;
    }

    @Produces
    @TopCommand
    @IfBuildProfile("prod")
    public Class<OtherEntryCommand> prodCommand() {
        return OtherEntryCommand.class;
    }
}
