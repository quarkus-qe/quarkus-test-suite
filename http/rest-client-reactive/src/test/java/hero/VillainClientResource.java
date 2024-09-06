package hero;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("villain-client-resource")
public class VillainClientResource {

    @RestClient
    VillainClient villainClient;

    @GET
    public Villain triggerClientToServerCommunication() {
        return villainClient.getRandomVillain();
    }

}
