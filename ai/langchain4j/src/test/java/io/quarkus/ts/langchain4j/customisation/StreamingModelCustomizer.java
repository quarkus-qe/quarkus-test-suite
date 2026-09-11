package io.quarkus.ts.langchain4j.customisation;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import io.quarkiverse.langchain4j.ModelBuilderCustomizer;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

@ApplicationScoped
public class StreamingModelCustomizer
        implements ModelBuilderCustomizer<OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder> {
    private static final Logger LOG = Logger.getLogger(StreamingModelCustomizer.class);

    @Override
    public void customize(OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder) {
        LOG.info("Customizing streaming chat model");
        builder
                .customHeaders(Map.of("X-QE-Header", "custom value"))
                .listeners(new ChatModelListener() {
                    @Override
                    public void onResponse(ChatModelResponseContext context) {
                        LOG.infof("Response from the streaming %s model", context.modelProvider().name());
                    }
                });
    }
}
