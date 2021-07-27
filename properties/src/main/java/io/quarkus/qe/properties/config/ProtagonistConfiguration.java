package io.quarkus.qe.properties.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.qe.properties.config.interfaces.ProtagonistConfigurable;

@Singleton
public class ProtagonistConfiguration {

    @Inject
    ProtagonistConfigurable protagonistConfigurable;

    public String getName() {
        return protagonistConfigurable.name();
    }

    public String getMessage() {
        return protagonistConfigurable.message();
    }

    public String getHobby() {
        return protagonistConfigurable.hobby();
    }

    public String getFriendName() {
        return protagonistConfigurable.friend().name();
    }

    public String getFriendMessage() {
        return protagonistConfigurable.friend().message();
    }
}
