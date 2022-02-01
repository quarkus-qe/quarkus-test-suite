package io.quarkus.ts.spring.web.openapi;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.smallrye.mutiny.Uni;

@RestController
@RequestMapping("/get")
public class GetController {

    private static final String HELLO_SPRING = "Hello Spring";

    @GetMapping(value = "/no-type")
    public Uni<String> getNoType() {
        return Uni.createFrom().item(HELLO_SPRING);
    }

    @GetMapping(value = "/text-plain", produces = MediaType.TEXT_PLAIN_VALUE)
    public Uni<String> getTextPlain() {
        return Uni.createFrom().item(HELLO_SPRING);
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Uni<String> getJson() {
        return Uni.createFrom().item(HELLO_SPRING);
    }

    @GetMapping(value = "/octet-stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Uni<String> getOctetStream() {
        return Uni.createFrom().item(HELLO_SPRING);
    }
}
