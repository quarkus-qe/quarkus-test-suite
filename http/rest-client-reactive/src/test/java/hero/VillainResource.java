package hero;

import java.util.random.RandomGenerator;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/api/villain/random")
public class VillainResource {

    @GET
    public Villain getRandomVillain() {
        long random = RandomGenerator.getDefault().nextLong();
        return new Villain(random, "Name-" + random, "Other-" + random, 1, "placeholder", "root");
    }

}
