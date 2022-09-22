package io.quarkus.ts.qute;

import io.quarkus.qute.i18n.Localized;
import io.quarkus.qute.i18n.Message;

@Localized("es")
public interface SpanishMessages extends Messages {

    @Override
    @Message("Hola, {name}!")
    String hello(String name);
}
