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

    @GetMapping("/fieldUsingValue")
    public String fieldUsingValue() {
        return fieldUsingValue;
    }

    @GetMapping("/fieldUsingArray")
    public String fieldUsingArray() {
        return Stream.of(fieldUsingArray).collect(Collectors.joining(", "));
    }

}
