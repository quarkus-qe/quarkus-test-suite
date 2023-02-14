package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.core.MediaType;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MediaTypeWrapper {

    private MediaType mediaType;

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
