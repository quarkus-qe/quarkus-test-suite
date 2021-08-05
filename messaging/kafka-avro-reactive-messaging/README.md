# kafka-avro-reactive-messaging

## How to run locally

From `kafka-avro-reactive-messaging` root folder 
 
* Launch all the required environment services: 

```
docker-compose -f src/main/resources/docker-compose-strimzi.yaml up
```

* Launch the app

```
mvn quarkus:dev
```

* Open your favorite browser and type your app main page `http://localhost:8080/prices.html`