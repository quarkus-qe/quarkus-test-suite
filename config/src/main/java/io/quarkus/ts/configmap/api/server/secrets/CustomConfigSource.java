package io.quarkus.ts.configmap.api.server.secrets;

import java.util.Map;

import io.smallrye.config.PropertiesConfigSource;

public class CustomConfigSource extends PropertiesConfigSource {
    public CustomConfigSource() {
        super(Map.of("secrets.custom-crypto-handler",
                "${aes-gcm-nopadding::DEaZok2mA76F-jak70kWav7Gx65QarcWbul-bvLgCHzy9eiHMkCWdadFISES2H7lewF61Ct-jqNaSQ}",
                "secrets.custom-base64-handler",
                "${custom-base64::cXVhcmt1cy1xZS1iYXNlNjQ=}",
                "secrets.custom-sha256-handler",
                "${sha256::0e8c2a8b8ecfbd52c3ef17acd44498ee2b892c66b308598f6a88ca8c7a235c4e}"),
                CustomConfigSource.class.getName(), 100);
    }
}
