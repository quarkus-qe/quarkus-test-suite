package io.quarkus.qe.qute.api;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import io.quarkus.arc.All;
import io.quarkus.qute.i18n.Localized;

@ApplicationScoped
public class AppMessagesProvider {

    private final AlertMessages enAppMessages;
    private final AlertMessages elAppMessages;

    @Inject
    public AppMessagesProvider(@Localized("en") @All List<AlertMessages> enAppMessages,
            @Localized("el") @Any @All List<AlertMessages> elAppMessages) {
        this.enAppMessages = enAppMessages.stream().findFirst().get();
        this.elAppMessages = elAppMessages.stream().findFirst().get();
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
