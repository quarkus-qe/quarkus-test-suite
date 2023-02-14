package io.quarkus.ts.configmap.api.server.secrets;

import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/properties")
public class InjectedProperitesResource extends SecretResource {
    @ConfigProperty(name = "secret.password")
    String password;

    @ConfigProperty(name = "the.answer")
    String answer;

    @ConfigProperty(name = "secret.ip")
    String ip;

    @Override
    public String getProperty(String key) {
        switch (key) {
            case "secret.password":
                return password;
            case "the.answer":
                return answer;
            case "secret.ip":
                return ip;
            default:
                throw new IllegalStateException("Unexpected value: " + key);
        }
    }
}
