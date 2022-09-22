package io.quarkus.ts.qute;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.qute.Qute;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.i18n.Localized;

@Path("")
public class Application {

    @Inject
    Template basic;

    @Location("1.i18n.html")
    Template multiLanguage;

    @Inject
    Engine engine;

    @Localized("he")
    Messages hebrew;

    @GET
    @Path("/basic")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance base() {
        return basic.data("server", "Quarkus");
    }

    @GET
    @Path("/location")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance located() {
        return multiLanguage.data("server", "Quarkus");
    }

    @GET
    @Path("/engine/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response engine(@PathParam("name") String name) {
        final Template template = engine.getTemplate(name);
        if (template == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(template.data("server", name + " engine")).build();
    }

    @POST
    @Path("/engine/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response registerNew(@PathParam("name") String name, String body) {
        Response.ResponseBuilder result;
        if (engine.getTemplate(name) == null) {
            engine.putTemplate(name, engine.parse(body));
            result = Response.created(getUri("/engine/" + name));
        } else {
            result = Response.status(Response.Status.CONFLICT);
        }
        return result.build();
    }

    @PUT
    @Path("/engine/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response register(@PathParam("name") String name, String body) {
        Template existing = engine.putTemplate(name, engine.parse(body));
        if (existing == null) {
            return Response.created(getUri("/engine/" + name)).build();
        } else {
            return Response.noContent().build();
        }
    }

    @DELETE
    @Path("/engine/{name}")
    @Produces(MediaType.TEXT_HTML)
    public Response delete(@PathParam("name") String name) {
        engine.removeTemplates(templateName -> templateName.equals(name));
        return Response.ok().build();
    }

    @GET
    @Path("/format")
    @Produces({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    public String format(@QueryParam("name") String name) {
        return Qute.fmt("This page is rendered for \"{}\" by Qute", name);
    }

    @GET
    @Path("/format-advanced")
    @Produces(MediaType.TEXT_HTML)
    public String formatHtml(@QueryParam("name") String name) {
        return Qute.fmt("This text is fluently rendered for \"{name}\" by Qute")
                .data("name", name)
                .render();
    }

    @GET
    @Path("/book")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance book() {
        Book musketeers = new Book("The Three Musketeers", "Alexandre Dumas", "d'Artagnan", "Athos", "Porthos", "Aramis");
        return engine.getTemplate("expressions")
                .data("server", "engine")
                .data("book", musketeers);
    }

    @GET
    @Path("/encoding")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance encoding() {
        return engine.getTemplate("нелатынь")
                .data("English", "hello")
                .data("česky", "čau")
                .data("по-русски", "привет")
                .data("בעברית", "שלום");
    }

    @GET
    @Path("/map")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance map(@QueryParam("name") @DefaultValue("islands") String region) {
        Map<String, String> map = new HashMap<>();
        if (region.equals("islands")) {
            map.put("Tasmania", "Hobart");
            map.put("Java", "Jakarta");
            map.put("The Great Britain", "London");
        }
        return engine
                .getTemplate("maps")
                .data("map", map);
    }

    @GET
    @Path("/inheritance")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance inheritance(@QueryParam("name") @DefaultValue("detail") String template) {
        return engine.getTemplate(template).instance();
    }

    @GET
    @Path("/annotated")
    public String annotated() {
        final Fish trout = new Fish("trout");
        return engine
                .getTemplate("annotations")
                .data("fish", trout)
                .data("wrapper", new StringWrapper("A quick brown fox jumps over the lazy dog"))
                .render();
    }

    @GET
    @Path("/enums/{city}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance enums(@PathParam("city") String city) {
        final City destination = City.valueOf(city.toUpperCase());
        return engine.parse("Good news, we will{#if city == City:BRUGES} not{/if} spend a week in {city.naturalName}!")
                .data("city", destination);
    }

    @GET
    @Path("/message/{locale}")
    @Produces(MediaType.TEXT_PLAIN)
    public TemplateInstance message(@PathParam("locale") String tag) {
        return engine
                .parse(String.format("{greeting:hello('%s')}", "Dr. Livingstone"))
                .instance()
                .setAttribute("locale", Locale.forLanguageTag(tag));
    }

    @GET
    @Path("/message/long/{locale}")
    @Produces(MediaType.TEXT_PLAIN)
    public TemplateInstance longMessage(@PathParam("locale") String tag) {
        return engine
                .parse(String.format("{greeting:long_hello('%s')}", "Dr. Livingstone"))
                .instance()
                .setAttribute("locale", Locale.forLanguageTag(tag));
    }

    @GET
    @Path("/message/")
    @Produces(MediaType.TEXT_PLAIN)
    public String injectedLocalization() {
        return hebrew.hello("אדם");
    }

    private static URI getUri(String path) {
        final Config system = ConfigProvider.getConfig();
        final String host = system.getValue("quarkus.http.host", String.class);
        final Integer port = system.getValue("quarkus.http.port", Integer.class);
        try {
            return new URL("http", host, port, path).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
