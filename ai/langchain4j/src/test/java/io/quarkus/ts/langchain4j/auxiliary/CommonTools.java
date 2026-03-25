package io.quarkus.ts.langchain4j.auxiliary;

import org.eclipse.microprofile.config.ConfigProvider;

public final class CommonTools {
    public static final String DEFAULT_ARGS = "--no-transfer-progress -DskipTests=true -DskipITs=true -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION} -Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID}";
    public static final String SAMPLE_BRANCH = "1.7";

    public static String getKey() {
        return ConfigProvider.getConfig().getValue("quarkus.langchain4j.openai.api-key", String.class);
    }
}
