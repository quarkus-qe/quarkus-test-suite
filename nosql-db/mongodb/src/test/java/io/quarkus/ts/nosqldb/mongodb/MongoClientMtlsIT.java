package io.quarkus.ts.nosqldb.mongodb;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.MongoDbService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.Mount;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-6233")
@QuarkusScenario
public class MongoClientMtlsIT extends AbstractMongoClientIT {

    @Container(image = "${mongodb.image}", port = 27017, expectedLog = "Waiting for connections", command = { "--config",
            "/etc/mongod.conf" }, mounts = { @Mount(from = "mongod.conf", to = "/etc/mongod.conf"),
                    @Mount(from = "mongo-certs/ca.pem", to = "/etc/ssl/ca.pem"),
                    @Mount(from = "mongo-certs/mongodb.pem", to = "/etc/ssl/mongodb.pem") }, portDockerHostToLocalhost = true)
    static MongoDbService database = new MongoDbService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("mtls.properties")
            .withProperty("quarkus.mongodb.connection-string", () -> database.getJdbcUrl());
}
