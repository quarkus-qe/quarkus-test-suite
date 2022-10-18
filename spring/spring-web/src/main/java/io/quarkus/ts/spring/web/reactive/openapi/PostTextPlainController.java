package io.quarkus.ts.spring.web.reactive.openapi;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/post-text-plain", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
public class PostTextPlainController {

    @PostMapping
    public String hello(@RequestBody String body) throws Exception {
        return StringEscapeUtils.escapeHtml4(body);
    }
}
