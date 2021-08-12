package io.quarkus.ts.spring.properties;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collections")
public class CollectionsController {

    // Using `@Inject` instead of `@Autowired` to verify we can use both.
    @Inject
    ListWiringProperties listProperties;

    // Injecting maps is not supported. Reported in https://github.com/quarkusio/quarkus/issues/19366
    //    @Autowired
    //    MapWiringProperties mapProperties;

    @GetMapping("/list/strings")
    public String listOfStrings() {
        return listProperties.strings.stream().collect(Collectors.joining(", "));
    }

    // Injecting lists with objects is not unsupported. Reported in https://github.com/quarkusio/quarkus/issues/19365
    //    @GetMapping("/list/persons")
    //    public String listOfPersons() {
    //        return listProperties.persons.stream().map(Object::toString).collect(Collectors.joining(", "));
    //    }
    //

    // Injecting maps is not supported. Reported in https://github.com/quarkusio/quarkus/issues/19366
    //    @GetMapping("/map/integers")
    //    public String mapOfIntegers() {
    //        return mapProperties.integers.entrySet().stream()
    //                .map(e -> e.getKey() + "=" + e.getValue())
    //                .collect(Collectors.joining(", "));
    //    }
    //
    //    @GetMapping("/map/persons")
    //    public String mapOfPersons() {
    //        return mapProperties.persons.entrySet().stream()
    //                .map(e -> e.getKey() + "=" + e.getValue())
    //                .collect(Collectors.joining(", "));
    //    }
}
