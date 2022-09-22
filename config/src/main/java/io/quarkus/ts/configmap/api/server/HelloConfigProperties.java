package io.quarkus.ts.configmap.api.server;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ConfigProperties(prefix = "hello")
public class HelloConfigProperties {

    @ConfigProperty(name = "message")
    Optional<String> message;

    @ConfigProperty(name = "preamble")
    String preamble;

    @ConfigProperty(name = "epilogue")
    String epilogue;

    @ConfigProperty(name = "side-note")
    String sideNote;

    public Optional<String> getMessage() {
        return message;
    }

    public String getPreamble() {
        return preamble;
    }

    public String getEpilogue() {
        return epilogue;
    }

    public String getSideNote() {
        return sideNote;
    }
}
