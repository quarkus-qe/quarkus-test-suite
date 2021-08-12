package io.quarkus.ts.spring.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greeting")
public class GreetingController {

    @Autowired
    GreetingProperties properties;

    @GetMapping("/text")
    public String text() {
        return properties.text;
    }

    @GetMapping("/textWithDefault")
    public String textWithDefault() {
        return properties.textWithDefault;
    }

    @GetMapping("/textPrivate")
    public String textPrivate() {
        return properties.getTextPrivate();
    }

    @GetMapping("/textOptional")
    public String textOptional() {
        return properties.textOptional.orElse("empty!");
    }

    @GetMapping("/message")
    public String message() {
        return properties.message.toString();
    }
}
