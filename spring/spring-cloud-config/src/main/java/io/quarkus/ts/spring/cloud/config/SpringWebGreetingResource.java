package io.quarkus.ts.spring.cloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/spring/hello")
public class SpringWebGreetingResource {

    @Value("${custom.message}")
    String message;

    @GetMapping
    public String hello() {
        return message;
    }
}