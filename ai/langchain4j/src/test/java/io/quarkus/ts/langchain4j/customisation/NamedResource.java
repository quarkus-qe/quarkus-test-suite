package io.quarkus.ts.langchain4j.customisation;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/named")
public class NamedResource {

    private final NamedBot bot;

    public NamedResource(NamedBot bot) {
        this.bot = bot;
    }

    @POST
    public String get(@DefaultValue("What can you do?") String message) {
        return bot.chat(message);
    }
}
