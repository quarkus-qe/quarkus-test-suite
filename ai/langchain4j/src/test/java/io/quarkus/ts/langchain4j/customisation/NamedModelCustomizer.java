package io.quarkus.ts.langchain4j.customisation;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkiverse.langchain4j.ModelBuilderCustomizer;
import io.quarkiverse.langchain4j.ModelName;

import dev.langchain4j.model.openai.OpenAiChatModel;

@ApplicationScoped
@ModelName("custom")
public class NamedModelCustomizer implements ModelBuilderCustomizer<OpenAiChatModel.OpenAiChatModelBuilder> {
    private static final Logger LOG = Logger.getLogger(NamedModelCustomizer.class);

    @Override
    public void customize(OpenAiChatModel.OpenAiChatModelBuilder builder) {
        LOG.info("Customizing single chat model");
        builder.customHeaders(Map.of("X-QE-Header", "only custom value"));
    }

    @Override
    public int priority() {
        return 100;
    }
}
