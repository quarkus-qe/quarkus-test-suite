package io.quarkus.ts.spring.web.reactive.openapi;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.smallrye.mutiny.Uni;

@RestController
@RequestMapping(value = "/get-text-plain", produces = MediaType.TEXT_PLAIN_VALUE)
public class GetTextPlainController {

    @GetMapping
    public Uni<String> getTextPlain() {
        return Uni.createFrom().item("Hello Spring");
    }
}
