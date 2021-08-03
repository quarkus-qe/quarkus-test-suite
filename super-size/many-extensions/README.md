# Many Extensions and Many Resources

Module is using 47 Quarkus extension and 60 REST endpoints.

The intention is to ensure such kind of application behaves well in various target environments. 

## Generated classes
```shell
SRC_PATH=/Users/rsvoboda/git/quarkus-test-suite/super-size/many-extensions/src/main/java/io/quarkus/ts/many/extensions
MAIN_TEST_PATH=/Users/rsvoboda/git/quarkus-test-suite/super-size/many-extensions/src/test/java/io/quarkus/ts/many/extensions/ManyExtensionsIT.java

for name in `project-name-generator -w 59 -o spaced`; do
  Name=`echo ${name:0:1} | tr  '[a-z]' '[A-Z]'`${name:1}
  echo "${name} - ${Name}"
  tee ${SRC_PATH}/Resource${Name}.java << EOF
package io.quarkus.ts.many.extensions;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;

@Path("/resource/${name}")
public class Resource${Name} {
    @Inject
    Service${Name} service;

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Hello process(@PathParam("name") String name) {
        return new Hello(service.process(name));
    }
}
EOF
  tee ${SRC_PATH}/Service${Name}.java << EOF
package io.quarkus.ts.many.extensions;
import javax.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class Service${Name} {
    public String process(String name) {
        return "${Name} - " + name + " - done";
    }
}
EOF

  tee -a ${MAIN_TEST_PATH} << EOF
    @Test
    public void testResource${Name}() {
        app.given().get("/api/resource/${name}/foo").then().statusCode(HttpStatus.SC_OK).body("content", is("${Name} - foo - done"));
    }
EOF
done


```