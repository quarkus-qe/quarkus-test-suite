package io.quarkus.ts.lifecycle;

import org.jboss.logging.Logger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public final class Main {
    private static final Logger LOG = Logger.getLogger(Main.class);
    private static final String ARGUMENTS_FROM_MAIN = "Received arguments: ";

    private Main() {

    }

    public static void main(String... args) {
        LOG.info(ARGUMENTS_FROM_MAIN + String.join(",", args));
        if (!(args.length > 0 && "cli".equals(args[0]))) {
            Quarkus.run(args);
        }
    }
}
