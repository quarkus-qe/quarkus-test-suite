package io.quarkus.ts.leak;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.ts.leak.scenarious.LeakScenario;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {

        if (args.length == 0) {
            throw new IllegalArgumentException("Leak test scenario must be specified");
        }

        LeakScenario scenario = LeakScenario.valueOf(args[0]);
        scenario.execute();

        return 0;
    }
}
