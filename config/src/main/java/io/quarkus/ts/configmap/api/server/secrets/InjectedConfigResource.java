package io.quarkus.ts.configmap.api.server.secrets;

import javax.inject.Inject;
import javax.ws.rs.Path;

import io.smallrye.config.SmallRyeConfig;

@Path("/injected")
public class InjectedConfigResource extends SecretResource {

    @Inject
    SmallRyeConfig config;

    @Override
    public String getProperty(String key) {
        return config.getRawValue(key);
    }
}
