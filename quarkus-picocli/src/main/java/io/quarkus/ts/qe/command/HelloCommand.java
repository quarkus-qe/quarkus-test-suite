package io.quarkus.ts.qe.command;

import jakarta.inject.Inject;

import io.quarkus.logging.Log;
import io.quarkus.ts.qe.services.HelloService;

import picocli.CommandLine;

@CommandLine.Command(name = "greeting", mixinStandardHelpOptions = true)
public class HelloCommand implements Runnable {
    @CommandLine.Option(names = { "-n", "--name" }, description = "Who will we greet?", defaultValue = "World")
    String name;
    @Inject
    HelloService helloService;

    public HelloCommand() {
    }

    public HelloCommand(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public void run() {
        Log.info("Executing HelloCommand with name: " + name);
        helloService.greet(name);
    }

}
