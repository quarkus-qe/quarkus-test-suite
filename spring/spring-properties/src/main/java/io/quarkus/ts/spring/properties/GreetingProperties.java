package io.quarkus.ts.spring.properties;

import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("greeting")
public class GreetingProperties {

    // Cover private field using public getter/setter
    private String textPrivate;

    // Cover optional fields
    public Optional<String> textOptional;

    // Cover direct field binding
    public String text;

    // Cover field with defaults
    public String textWithDefault = "Hola";

    // Cover group fields
    public NestedMessage message;

    public String getTextPrivate() {
        return textPrivate;
    }

    public void setTextPrivate(String textPrivate) {
        this.textPrivate = textPrivate;
    }

    public static class NestedMessage {
        public String text;
        public String person = "unknown";

        @Override
        public String toString() {
            return text + " " + person + "!";
        }
    }
}