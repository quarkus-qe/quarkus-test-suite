package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateEnum;

@TemplateEnum
public enum City {
    BRUGES,
    DUBLIN,
    MOMBASA;

    public String naturalName() {
        String full = this.name();
        String firstLetter = full.substring(0, 1);
        String rest = full.substring(1).toLowerCase();
        return firstLetter + rest;
    }
}
