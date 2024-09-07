package hero;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "villain")
public interface VillainClient {

    @GET
    @Path("/api/villain/random")
    Villain getRandomVillain();

}
