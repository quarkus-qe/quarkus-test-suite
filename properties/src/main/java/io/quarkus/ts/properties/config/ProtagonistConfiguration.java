package io.quarkus.ts.properties.config;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkus.ts.properties.config.interfaces.ProtagonistConfigurable;

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
