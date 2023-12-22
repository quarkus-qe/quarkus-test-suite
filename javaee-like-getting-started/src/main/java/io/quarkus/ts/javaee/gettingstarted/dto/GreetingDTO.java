package io.quarkus.ts.javaee.gettingstarted.dto;

public class GreetingDTO {

    private final String greeting;

    public GreetingDTO(String hello, String from, String to) {
        this.greeting = String.join(" ", hello, to, "from", from);
    }

    public String getGreeting() {
        return greeting;
    }
}
