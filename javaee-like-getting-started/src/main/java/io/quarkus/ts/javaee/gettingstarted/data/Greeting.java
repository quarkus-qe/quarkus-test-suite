package io.quarkus.ts.javaee.gettingstarted.data;

public class Greeting {

    private final String hello;
    private final From from;
    private final To to;

    public Greeting(String hello, String from, String to) {
        this.hello = hello;
        this.from = new From(from);
        this.to = new To(to);
    }

    public String getHello() {
        return hello;
    }

    public From getFrom() {
        return from;
    }

    public To getTo() {
        return to;
    }

    public record From(String data) {
    }

    public record To(String data) {
    }

}
