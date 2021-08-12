package io.quarkus.ts.spring.properties;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/values")
public class ValueController {

    @Value("${values.text}")
    String fieldUsingValue;

    @Value("${values.list}")
    String[] fieldUsingArray;

    // Complex SpEL expresions is not supported: https://github.com/quarkusio/quarkus/issues/19368"
    //    @Value("#{'${values.list}'.split(',')}")
    //    List<String> fieldUsingList;

    // Complex SpEL expresions is not supported: https://github.com/quarkusio/quarkus/issues/19368"
    //    @Value("#{${values.map}}")
    //    Map<String, String> fieldUsingMap;

    @GetMapping("/fieldUsingValue")
    public String fieldUsingValue() {
        return fieldUsingValue;
    }

    @GetMapping("/fieldUsingArray")
    public String fieldUsingArray() {
        return Stream.of(fieldUsingArray).collect(Collectors.joining(", "));
    }

    // Complex SpEL expresions is not supported: https://github.com/quarkusio/quarkus/issues/19368"
    //    @GetMapping("/fieldUsingList")
    //    public String fieldUsingList() {
    //        return fieldUsingList.stream().collect(Collectors.joining(", "));
    //    }
    //
    //    @GetMapping("/fieldUsingMap")
    //    public String fieldUsingMap() {
    //        return fieldUsingMap.entrySet().stream()
    //                .map(e -> e.getKey() + ": " + e.getValue())
    //                .collect(Collectors.joining(", "));
    //    }

}
