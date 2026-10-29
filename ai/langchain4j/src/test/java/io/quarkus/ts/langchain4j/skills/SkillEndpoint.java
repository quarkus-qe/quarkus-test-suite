package io.quarkus.ts.langchain4j.skills;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.skills.Skills;
import io.smallrye.common.annotation.Blocking;

@Path("/skills")
public class SkillEndpoint {

    @RegisterAiService
    public interface CityInfoService {
        @Skills
        String weatherChat(String message);

        // this one is not using @Skills, so they should not be loaded
        String geographyChat(String message);
    }

    @Inject
    CityInfoService cityInfoService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    @Path("/temperature/{city}")
    public String temperature(@PathParam("city") String city) {
        return cityInfoService.weatherChat("What is the current temperature in %s?".formatted(city));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    @Path("/country/{city}")
    public String country(@PathParam("city") String city) {
        return cityInfoService.geographyChat("What is the name of the country in which %s is located?".formatted(city));
    }

    @RegisterAiService(toolProviderSupplier = ProgrammaticToolProvider.class, systemMessageProviderSupplier = ProgrammaticToolProvider.class)
    public interface AppInfo {
        String ask(String message);
    }

    @Inject
    AppInfo appInfo;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    @Path("/info")
    public String question(String question) {
        return appInfo.ask(question);
    }
}
