package io.quarkus.qe.qute.api;

import io.quarkus.qute.i18n.Message;
import io.quarkus.qute.i18n.MessageBundle;

@MessageBundle(value = "alert")
public interface AlertMessages {

    @Message("Hello")
    String withoutParams();

    @Message("Hello {name}")
    String withParams(String name);
}
