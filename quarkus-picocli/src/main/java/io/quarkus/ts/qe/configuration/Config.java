package io.quarkus.ts.qe.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.ts.qe.command.EntryCommand;
import io.quarkus.ts.qe.command.OtherEntryCommand;

@ApplicationScoped
public class Config {
    @Produces
    @TopCommand
    @IfBuildProfile("dev")
    public Object devCommand() {
        return EntryCommand.class;
    }

    @Produces
    @TopCommand
    @IfBuildProfile("prod")
    public Object prodCommand() {
        return OtherEntryCommand.class;
    }

}