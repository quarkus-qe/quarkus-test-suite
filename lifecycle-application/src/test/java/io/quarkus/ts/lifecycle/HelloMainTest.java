package io.quarkus.ts.lifecycle;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class HelloMainTest {

    @Test
    @Launch({ "cli", "Hello", "World" })
    public void annotatedLaunch(LaunchResult result) {
        List<String> outputStream = result.getOutputStream();
        String args = null;
        for (String output : outputStream) {
            if (output.contains("Received arguments:")) {
                args = output;
            }
        }
        Assertions.assertNotNull(args);
        Assertions.assertTrue(args.contains("Hello"), "No 'Hello' in the output!");
        Assertions.assertTrue(args.contains("World"), "No 'World' in the output!");
    }
}
