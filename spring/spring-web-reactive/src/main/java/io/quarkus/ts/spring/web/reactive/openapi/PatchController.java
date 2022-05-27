package io.quarkus.ts.spring.web.reactive.openapi;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.smallrye.mutiny.Uni;

@RestController
@RequestMapping(value = "/patch")
public class PatchController {

    @PatchMapping(value = "/no-type")
    public Uni<String> postNoType(@RequestBody String body) {
        return Uni.createFrom().item(body);
    }

    @PatchMapping(value = "/text-plain", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Uni<String> postTextPlain(@RequestBody String body) {
        return Uni.createFrom().item(body);
    }

    @PatchMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Uni<String> postJson(@RequestBody String body) {
        return Uni.createFrom().item(body);
    }

    @PatchMapping(value = "/octet-stream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Uni<String> postOctetStream(@RequestBody String body) {
        return Uni.createFrom().item(body);
    }
}
