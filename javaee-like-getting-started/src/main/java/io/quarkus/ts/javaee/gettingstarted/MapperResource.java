package io.quarkus.ts.javaee.gettingstarted;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.quarkus.ts.javaee.gettingstarted.data.ChitChat;
import io.quarkus.ts.javaee.gettingstarted.data.Farewell;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneDefaultMethod;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneHappyImpl;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartOneSadImpl;
import io.quarkus.ts.javaee.gettingstarted.data.FarewellPartTwo;
import io.quarkus.ts.javaee.gettingstarted.data.Greeting;
import io.quarkus.ts.javaee.gettingstarted.data.SeriousTalk;
import io.quarkus.ts.javaee.gettingstarted.dto.ChitChatDTO;
import io.quarkus.ts.javaee.gettingstarted.dto.FarewellDTO;
import io.quarkus.ts.javaee.gettingstarted.dto.GreetingDTO;
import io.quarkus.ts.javaee.gettingstarted.dto.SeriousTalkDTO;
import io.quarkus.ts.javaee.gettingstarted.mapper.ChitChatMapper;
import io.quarkus.ts.javaee.gettingstarted.mapper.FarewellMapper;
import io.quarkus.ts.javaee.gettingstarted.mapper.GreetingMapper;
import io.quarkus.ts.javaee.gettingstarted.mapper.SeriousTalkMapper;

@Path("/mapper")
public class MapperResource {
    private final ChitChatMapper chitChatMapper;

    @Inject
    GreetingMapper greetingMapper;

    @Named("farewellMapperImpl")
    @Inject
    FarewellMapper farewellMapper;

    public MapperResource(ChitChatMapper chitChatMapper) {
        this.chitChatMapper = chitChatMapper;
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("injected-jakarta-cdi")
    public GreetingDTO injectedJakartaCdi() {
        return greetingMapper.map(new Greeting("Hello", "Maximillian", "Sebastian"));
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("injected-jakarta-happy-impl")
    public FarewellDTO injectedJakartaHappyImpl() {
        return farewellMapper.map(new Farewell(new FarewellPartOneHappyImpl("happy"), new FarewellPartTwo(1)));
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("injected-jakarta-sad-impl")
    public FarewellDTO injectedJakartaSadImpl() {
        return farewellMapper.map(new Farewell(new FarewellPartOneSadImpl(), new FarewellPartTwo(2)));
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("injected-jakarta-default-method")
    public FarewellDTO injectedJakartaDefaultMethod() {
        return farewellMapper.map(new Farewell(new FarewellPartOneDefaultMethod(), new FarewellPartTwo(3)));
    }

    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_JSON)
    @POST
    @Path("injected-cdi")
    public ChitChatDTO injectedCdi(ChitChat chitChat) {
        return chitChatMapper.map(chitChat);
    }

    @Produces(APPLICATION_JSON)
    @GET
    @Path("no-cdi")
    public SeriousTalkDTO noCdi() {
        return SeriousTalkMapper.INSTANCE.map(new SeriousTalk("How", "do you", "do?"));
    }
}
