package io.quarkus.ts.qute;

import io.quarkus.qute.TemplateGlobal;

@TemplateGlobal
public class Variables {
    static final int airspeedVelocityOfAnUnladenSwallow = 11;

    @TemplateGlobal(name = "random")
    static int getRandomNumber() {
        return 5;
    }
}
