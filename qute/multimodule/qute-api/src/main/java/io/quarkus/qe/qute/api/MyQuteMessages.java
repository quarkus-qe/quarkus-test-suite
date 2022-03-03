package io.quarkus.qe.qute.api;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle
public interface MyQuteMessages {
    @Message("Hello world!")
    String hello();

    @Message("Hello {name}!")
    String hello_name(String name);
}
