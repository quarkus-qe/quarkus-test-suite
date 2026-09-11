package io.quarkus.ts.langchain4j.customisation;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkiverse.langchain4j.ModelBuilderCustomizer;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.openai.OpenAiChatModel;

@ApplicationScoped
public class PlainModelCustomizer implements ModelBuilderCustomizer<OpenAiChatModel.OpenAiChatModelBuilder> {
    private static final Logger LOG = Logger.getLogger(PlainModelCustomizer.class);

    @Override
    public void customize(OpenAiChatModel.OpenAiChatModelBuilder builder) {
        LOG.info("Customizing chat model");
        builder
                .customHeaders(Map.of("X-QE-Header", "another custom value"))
                .listeners(new ChatModelListener() {
                    @Override
                    public void onResponse(ChatModelResponseContext context) {
                        LOG.infof("Response from %s model", context.modelProvider().name());
                    }
                });
    }
}
