package io.quarkus.ts.spring.web.reactive.openapi;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/post")
public class PostController {

    @PostMapping(value = "/no-type")
    public String postNoType(@RequestBody String body) throws Exception {
        return StringEscapeUtils.escapeHtml4(body);
    }

    @PostMapping(value = "/text-plain", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String postTextPlain(@RequestBody String body) {
        return body;
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String postJson(@RequestBody String body) {
        return body;
    }

    @PostMapping(value = "/octet-stream", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String postOctetStream(@RequestBody String body) {
        return body;
    }
}
