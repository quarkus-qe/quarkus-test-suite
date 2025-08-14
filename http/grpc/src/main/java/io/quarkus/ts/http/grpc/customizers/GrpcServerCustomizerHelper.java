package io.quarkus.ts.http.grpc.customizers;

import jakarta.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.Context;
import io.grpc.Metadata;

@Singleton
public final class GrpcServerCustomizerHelper {

    private final Metadata.Key<InterceptorInvocations> METADATA_KEY = Metadata.Key.of("grpc-interceptor-invocations",
            new Metadata.AsciiMarshaller<>() {

                private final ObjectMapper objectMapper = new ObjectMapper();

                @Override
                public String toAsciiString(InterceptorInvocations interceptorInvocations) {
                    try {
                        return objectMapper.writeValueAsString(interceptorInvocations);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public InterceptorInvocations parseAsciiString(String s) {
                    try {
                        return objectMapper.readValue(s, InterceptorInvocations.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

    private final Context.Key<InterceptorInvocations> CONTEXT_KEY = Context.key("grpc-interceptor-invocations");

    Context putMetadataInvocationsToContext(Metadata metadata) {
        InterceptorInvocations invocations = getInvocationsFromMetadata(metadata);
        return Context.current().withValue(CONTEXT_KEY, invocations);
    }

    void recordInterceptorInvocation(Metadata metadata, String customizerName) {
        InterceptorInvocations invocations = getInvocationsFromMetadata(metadata).withInvocation(customizerName);
        metadata.put(METADATA_KEY, invocations);
    }

    InterceptorInvocations getInvocationsFromContext() {
        return CONTEXT_KEY.get();
    }

    private InterceptorInvocations getInvocationsFromMetadata(Metadata metadata) {
        InterceptorInvocations interceptorInvocations = metadata.get(METADATA_KEY);
        if (interceptorInvocations == null) {
            interceptorInvocations = new InterceptorInvocations();
        }
        return interceptorInvocations;
    }
}
