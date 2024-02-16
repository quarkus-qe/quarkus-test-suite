package io.quarkus.ts.http.pact;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlakyTest {

    @Test
    public void failOnce() throws IOException {
        Path touch = Path.of(".").resolve("target").resolve("touch");
        if (!Files.exists(touch)) {
            touch.toFile().createNewFile();
            Assertions.fail("Failing as this is first attempt");
        }
    }

}
