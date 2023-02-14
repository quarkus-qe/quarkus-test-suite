package io.quarkus.ts.qute.api;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.i18n.Localized;
import io.quarkus.qute.i18n.MessageBundles;

@Path("/hello")
public class MyQuteResource {
    private final Template page;

    @Inject
    MyQuteMessages enAppMessages;
    @Localized("cs")
    MyQuteMessages csAppMessages;

    @Inject
    AlertMessages enAlertMessages;
    @Localized("el")
    AlertMessages elAlertMessages;

    public MyQuteResource(Template page) {
        this.page = requireNonNull(page, "page is required");
    }

    @GET
    @Path("{lang}")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("lang") String lang) {
        AlertMessages messages = lang.equals("el") ? elAlertMessages : enAlertMessages;
        return messages.withoutParams() + ". " + messages.withParams("Nikos");
    }

    @GET
    @Path("/names")
    @Produces(MediaType.TEXT_PLAIN)
    public String names() {
        return MessageBundles.get(MyQuteMessages.class).hello_name("Rostislav") + " == " +
                enAppMessages.hello_name("Rostislav") + " | " +
                MessageBundles.get(MyQuteMessages.class, Localized.Literal.of("cs")).hello_name("Rostislav") + " == " +
                csAppMessages.hello_name("Rostislav");
    }

    @GET
    @Path("/page")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        return page.data("name", name).setAttribute("locale", Locale.forLanguageTag("cs"));
    }
}
