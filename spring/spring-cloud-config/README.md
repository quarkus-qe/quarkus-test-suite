# Quarkus Spring Cloud Config

1. Start Spring Cloud Server from Docker

We'll start a Spring Cloud Server instance using a property file placed at `src/test/resources`:

```
docker run -v /src/test/resources:/config -e SPRING_PROFILES_ACTIVE=native -p 8888:8888 hyness/spring-cloud-config-server
```

Note that the property file must be named as `{quarkus.application.name}-{quarkus.profile}.properties` (YAML is also allowed).

To verify the Spring Cloud Server is up and running: `curl http://localhost:8888/{quarkus.application.name}/{quarkus.profile}` 
(If you're using the file `application-SpringCloudConfigIT.properties`, then `curl http://localhost:8888/application/SpringCloudConfigIT`). 
And you should get:

```
{
    "name": "application",
    "profiles": [
        "SpringCloudConfigIT"
    ],
    "label": null,
    "version": null,
    "state": null,
    "propertySources": [
        {
            "name": "file:config/application-SpringCloudConfigIT.properties",
            "source": {
                "custom.message": "Hello from Spring Cloud Server"
            }
        }
    ]
}
``` 

2. Start the Quarkus application

```
mvn -Dquarkus.profile=SpringCloudConfigIT quarkus:dev
```

Note that I used the profile `SpringCloudConfigIT` to use the property file called `application-SpringCloudConfigIT.properties`.

Now, go to `http://localhost:8080/jaxrs/hello` and you should get `Hello from Spring Cloud Server`.
