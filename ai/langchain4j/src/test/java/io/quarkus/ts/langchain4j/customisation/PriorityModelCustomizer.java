package io.quarkus.ts.langchain4j.customisation;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkiverse.langchain4j.ModelBuilderCustomizer;

import dev.langchain4j.model.openai.OpenAiChatModel;

@ApplicationScoped
public class PriorityModelCustomizer implements ModelBuilderCustomizer<OpenAiChatModel.OpenAiChatModelBuilder> {
    private static final Logger LOG = Logger.getLogger(PriorityModelCustomizer.class);

    @Override
    public void customize(OpenAiChatModel.OpenAiChatModelBuilder builder) {
        LOG.info("Customizing priority chat model");
        builder
                .customHeaders(Map.of("X-QE-Header", "first custom value"))
                .frequencyPenalty(0.5);
    }

    @Override
    public int priority() {
        return 1;
    }
}
