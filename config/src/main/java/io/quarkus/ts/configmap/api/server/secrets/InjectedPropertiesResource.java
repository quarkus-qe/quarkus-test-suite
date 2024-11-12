package io.quarkus.ts.configmap.api.server.secrets;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

@Path("/properties")
public class InjectedPropertiesResource extends SecretResource {

    public static final String SECRETS_PREFIX = "secrets.";

    @ConfigProperty(name = "secret.password")
    String password;

    @ConfigProperty(name = "the.answer")
    String answer;

    @ConfigProperty(name = "secret.ip")
    String ip;

    @ConfigProperty(name = "plain-keystore-config-key")
    String plainKeystoreConfigValue;

    @Inject
    Secrets secrets;

    @Override
    public String getProperty(String key) {
        return switch (key) {
            case "secret.password" -> password;
            case "the.answer" -> answer;
            case "secret.ip" -> ip;
            case "plain-keystore-config-key" -> plainKeystoreConfigValue;
            default -> {
                if (key.startsWith(SECRETS_PREFIX)) {
                    var secretKey = key.substring(SECRETS_PREFIX.length());
                    if (secrets.secretKeyToValue().containsKey(secretKey)) {
                        yield secrets.secretKeyToValue().get(secretKey);
                    }
                }
                throw new IllegalStateException("Unexpected value: " + key);
            }
        };
    }

    @ConfigMapping(prefix = "secrets")
    public interface Secrets {

        @WithParentName
        Map<String, String> secretKeyToValue();

    }
}
