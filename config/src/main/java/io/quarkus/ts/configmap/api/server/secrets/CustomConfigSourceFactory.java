package io.quarkus.ts.configmap.api.server.secrets;

import java.util.List;
import java.util.Properties;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.PropertiesConfigSource;

public class CustomConfigSourceFactory implements ConfigSourceFactory {
    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext configSourceContext) {
        var properties = new Properties();
        properties.setProperty("secrets.custom-factory-crypto-handler",
                "${aes-gcm-nopadding::DEaZok2mA76F-jak70kWav7Gx65QarcWbul-bvLgCHzy9eiHMkCWdadFISES2H7lewF61Ct-jqNaSQ}");
        properties.setProperty("secrets.custom-factory-base64-handler",
                "${custom-base64::cXVhcmt1cy1xZS1iYXNlNjQ=}");
        properties.setProperty("secrets.custom-factory-sha256-handler",
                "${sha256::0e8c2a8b8ecfbd52c3ef17acd44498ee2b892c66b308598f6a88ca8c7a235c4e}");
        return List.of(new PropertiesConfigSource(properties, CustomConfigSourceFactory.class.getName()));
    }
}
