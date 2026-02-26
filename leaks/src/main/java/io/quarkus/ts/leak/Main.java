package io.quarkus.ts.leak;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Intentionally stress Jackson TypeFactory cache
        for (int i = 0; i < 500; i++) {
            mapper.readValue("{\"name\":\"John\"}", Person.class);
        }

        return 0;
    }

    public static class Person {
        public String name;
    }
}
