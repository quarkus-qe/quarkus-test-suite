package io.quarkus.ts.qute;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle("greeting")
public interface Messages {
    @Message("Hello, {name}!")
    String hello(String name);

    @Message("Hello, {name}! \n How are you, {name}?")
    String long_hello(String name);

}
