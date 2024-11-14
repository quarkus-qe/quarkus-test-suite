package io.quarkus.ts.spring.properties;

import jakarta.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collections")
public class CollectionsController {

    @Inject // Using `@Inject` instead of `@Autowired` to verify we can use both.
    ListWiringProperties listProperties;

    @GetMapping("/list/strings")
    public String listOfStrings() {
        return String.join(", ", listProperties.strings);
    }
}
