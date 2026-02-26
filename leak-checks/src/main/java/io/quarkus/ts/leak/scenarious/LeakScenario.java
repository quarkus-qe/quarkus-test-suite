package io.quarkus.ts.leak.scenarious;

public enum LeakScenario {

    JACKSON {
        @Override
        public void execute() throws Exception {
            JacksonClassLoaderLeakScenario.execute();
        }
    };

    public abstract void execute() throws Exception;
}
