package io.quarkus.ts.qute;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Location;
import io.quarkus.qute.Qute;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.i18n.Localized;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

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

    @GET
    @Path("/record-order/test")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance recordComplexOrderTest() {
        return new UserTemplates.UserWithOrder(123, "Test User", 789L, true);
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
    public CompletionStage<String> book() {
        Book musketeers = new Book("The Three Musketeers", "Alexandre Dumas", "d'Artagnan", "Athos", "Porthos", "Aramis");
        return engine.getTemplate("expressions")
                .data("server", "engine")
                .data("book", musketeers)
                .renderAsync();
    }

    @POST
    @Path("/book/object")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<String> book(Book book) {
        String template = """
                The book is called {book.title} and is written by {book.['author']}
                It has {book.characters.length} characters:
                {#for character in book.characters}
                {character_count}. {character}{#if character_hasNext} {#if character_odd }as well as{/if}{#if character_even }and also{/if}{/if}
                {/for}
                """;
        return Qute.fmt(template)
                .data("server", "engine")
                .data("book", book)
                .instance().createUni();
    }

    @POST
    @Path("/book/json")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<String> book(JsonObject book) {
        String template = """
                The book is called {book.getString('title')} and is written by {book.['author']}
                It has {book.characters.size} characters:
                {#for character in book.characters}
                {character_count}. {character}{#if character_hasNext} {#if character_odd }as well as{/if}{#if character_even }and also{/if}{/if}
                {/for}
                """;
        return Qute.fmt(template)
                .data("server", "engine")
                .data("book", book)
                .instance().createUni();
    }

    @GET
    @Path("/encoding")
    @Produces(MediaType.TEXT_HTML)
    public Uni<String> encoding() {
        return engine.getTemplate("нелатынь")
                .data("English", "hello")
                .data("česky", "čau")
                .data("по-русски", "привет")
                .data("בעברית", "שלום")
                .createUni();
    }

    @GET
    @Path("/map")
    @Produces(MediaType.TEXT_HTML)
    public Multi<String> map(@QueryParam("name") @DefaultValue("islands") String region) {
        Map<String, String> map = new HashMap<>();
        if (region.equals("islands")) {
            map.put("Tasmania", "Hobart");
            map.put("Java", "Jakarta");
            map.put("The Great Britain", "London");
        }
        return engine
                .getTemplate("maps")
                .data("map", map)
                .createMulti();
    }

    @GET
    @Path("/inheritance")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance inheritance(@QueryParam("name") @DefaultValue("detail") String template) {
        return engine.getTemplate(template).instance();
    }

    @GET
    @Path("/annotated")
    public TemplateInstance annotated() {
        final Fish trout = new Fish("trout");
        return engine
                .getTemplate("annotations")
                .data("fish", trout)
                .data("wrapper", new StringWrapper("A quick brown fox jumps over the lazy dog"));
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
    public Uni<String> longMessage(@PathParam("locale") String tag) {
        return engine
                .parse(String.format("{greeting:long_hello('%s')}", "Dr. Livingstone"))
                .instance()
                .setAttribute("locale", Locale.forLanguageTag(tag))
                .createUni();
    }

    @GET
    @Path("/message/")
    @Produces(MediaType.TEXT_PLAIN)
    public String injectedLocalization() {
        return hebrew.hello("אדם");
    }

    @GET
    @Path("/class/{name}")
    public Response getClass(@PathParam("name") String className) {
        try {
            Class<?> existingclass = Class.forName(className);
            return Response.ok(existingclass.getCanonicalName()).build();
        } catch (ClassNotFoundException e) {
            return Response.status(Response.Status.NO_CONTENT.getStatusCode(),
                    "There is no such class: " + className).build();
        }
    }

    private static URI getUri(String path) {
        final Config system = ConfigProvider.getConfig();
        final String host = system.getValue("quarkus.http.host", String.class);
        final Integer port = system.getValue("quarkus.http.port", Integer.class);
        try {
            return new URI("http", null, host, port, path, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
