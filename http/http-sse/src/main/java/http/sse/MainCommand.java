package http.sse;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.Quarkus;

import picocli.CommandLine;

@TopCommand
@CommandLine.Command(name = "test")
public class MainCommand implements Runnable {
    @Override
    public void run() {
        Quarkus.waitForExit();
    }
}
