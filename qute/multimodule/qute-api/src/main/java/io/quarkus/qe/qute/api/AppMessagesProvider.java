package io.quarkus.qe.qute.api;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;

import io.quarkus.qute.i18n.Localized;

@ApplicationScoped
public class AppMessagesProvider {

    private final AlertMessages enAppMessages;
    private final AlertMessages elAppMessages;

    public AppMessagesProvider(@Localized("en") Instance<AlertMessages> enAppMessages,
            @Localized("el") Instance<AlertMessages> elAppMessages) {
        this.enAppMessages = enAppMessages.iterator().next();
        this.elAppMessages = elAppMessages.iterator().next();
    }

    public AlertMessages appMessages(String lang) {
        switch (lang) {
            case "el":
                return elAppMessages;
            case "en":
            default:
                return enAppMessages;
        }
    }
}
