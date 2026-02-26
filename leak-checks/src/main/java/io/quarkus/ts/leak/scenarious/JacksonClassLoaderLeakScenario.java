package io.quarkus.ts.leak.scenarious;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Scenario that triggers Jackson TypeFactory usage during
 * repeated QuarkusMain lifecycles to verify correct classloader cleanup
 */
public final class JacksonClassLoaderLeakScenario {

    private JacksonClassLoaderLeakScenario() {
    }

    public static void execute() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 500; i++) {
            mapper.readValue("{\"name\":\"John\"}", Person.class);
        }
    }

    public static class Person {
        public String name;
    }
}
