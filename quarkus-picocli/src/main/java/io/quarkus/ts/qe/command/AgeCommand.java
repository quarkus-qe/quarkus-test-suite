package io.quarkus.ts.qe.command;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.logging.Log;

import picocli.CommandLine;

/**
 * According picocli guide,
 * Beans with @CommandLine.Command should not use proxied scopes (e.g. do not use @ApplicationScope)
 * because Picocli will not be able to set field values in such beans.
 * So this class creation is to test it, see testAgeCommandWithApplicationScoped.
 */
@CommandLine.Command(name = "age", mixinStandardHelpOptions = true)
@ApplicationScoped
public class AgeCommand implements Runnable {
    @CommandLine.Option(names = { "-a", "--age" }, description = "your age", defaultValue = "0")
    private int age;

    @Override
    public void run() {
        Log.info("Your age is: " + age);
    }
}
