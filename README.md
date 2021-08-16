# Quarkus Test Suite

The test suite includes:
- Bare metal scenario testing
- OpenShift scenario testing

For running the tests in OpenShift, it is expected that the user is logged into an OpenShift project:
- with OpenShift 4, run `oc login https://api.<cluster name>.<domain>:6443`

To verify that you're logged into correct project, you can run `oc whoami` and `oc project`.

Running the tests amounts to standard `mvn clean verify`.
This will use a specific Quarkus version, which can be modified by setting the `quarkus.platform.version` property.
Alternatively, you can use `-Dquarkus-core-only` to run the test suite against Quarkus `999-SNAPSHOT`.
In that case, make sure you have built Quarkus locally prior to running the tests.

Since this is standard Quarkus configuration, it's possible to override using a system property.
Therefore, if you want to run the tests with a different Java S2I image, run `mvn clean verify -Dquarkus.s2i.base-jvm-image=...`.

### Other prerequisites

In addition to common prerequisites for Java projects (JDK, Maven) and OpenShift (`oc`), the following utilities must also be installed on the machine where the test suite is being executed:

- an OpenShift instance where is enabled the monitoring for user-defined projects (see [documentation](https://docs.openshift.com/container-platform/4.6/monitoring/enabling-monitoring-for-user-defined-projects.html))
- the OpenShift user must have permission to create ServiceMonitor CRDs and access to the `openshift-user-workload-monitoring` namespace:

For example, if the OpenShift user is called `qe`, then we should have this cluster role assigned to it:

```
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: monitor-crd-edit
rules:
- apiGroups: ["monitoring.coreos.com", "apiextensions.k8s.io"]
  resources: ["prometheusrules", "servicemonitors", "podmonitors", "customresourcedefinitions"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
EOF
```

And also, the same user `qe` should have access to the `openshift-user-workload-monitoring` namespace (this namespace is automatically created by the Prometheus operator when enabling the monitoring of user-defined projects):

```
oc adm policy add-role-to-user edit qe -n openshift-user-workload-monitoring
```

These requirements are necessary to verify the `micrometer/prometheus` and `micrometer/prometheus-kafka` tests. 

- the OpenShift user must have permission to create Operators:

```
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: install-operators-role
rules:
- apiGroups: ["operators.coreos.com"]
  resources: ["operatorgroups"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
```

These requirements are necessary to verify the tests with tag `include-operator-scenarios`.

## Running against Red Hat build of Quarkus

When running against released Red Hat build of Quarkus make sure https://maven.repository.redhat.com/ga/ repository is defined in settings.xml.

Example command for released Red Hat build of Quarkus:
```
mvn -fae clean verify \
 -Dts.redhat.registry.enabled \
 -Dversion.quarkus=1.3.4.Final-redhat-00004 \
 -Dquarkus.platform.group-id=com.redhat.quarkus
```

Example command for not yet released version of Red Hat build of Quarkus:

```
mvn -fae clean verify \
 -Dts.redhat.registry.enabled \
 -Dversion.quarkus=1.7.1.Final-redhat-00001 \
 -Dquarkus.platform.group-id=com.redhat.quarkus \
 -Dmaven.repo.local=/Users/rsvoboda/Downloads/rh-quarkus-1.7.1.GA-maven-repository/maven-repository
```

## Branching Strategy

The `main` branch is always meant for latest upstream/downstream development. For each downstream major.minor version, there's a corresponding maintenance branch:

  - `1.11` for Red Hat build of Quarkus 1.11.z (corresponding upstream version: `1.11.0.Final+`)

## Test Framework

We use a Quarkus QE Test Framework to verify this test suite. For further information about it, please go to [here](https://github.com/quarkus-qe/quarkus-test-framework).

## Name Convention 

For bare metal testing, test classes must be named `*IT`, executed by Failsafe.
OpenShift tests should be named `OpenShift*IT`.
DevMode tests should be named `DevMode*IT`.

### OpenShift Serverless / Knative

The test suite contains a Maven profile activated using the `include.serverless` property or `serverless` profile name.
This profile includes additional modules with serverless test coverage into the execution of the testsuite.
Serverless test coverage supports both JVM and Native mode.

The following command will execute the whole test suite including serverless tests:

```
./mvnw clean verify -Dinclude.serverless
```

### OpenShift Operators

The test suite contains a Maven profile activated using the `include.operator-scenarios` property or `operator-scenarios` profile name.
This profile includes additional modules with serverless test coverage into the execution of the testsuite.
Serverless test coverage supports both JVM and Native mode.

The following command will execute the whole test suite including serverless tests:

```
./mvnw clean verify -Dinclude.operator-scenarios
```

## Existing tests

### `http-minimum`
Verifies that you can deploy a simple HTTP endpoint to OpenShift and access it.
It also verifies multiple deployment strategies like:
- Serverless
- Using OpenShift quarkus extension
- Using OpenShift quarkus extension and Docker Build strategy

### `http-advanced`
Verifies Server/Client http_2/1.1, Grpc and http redirections.


### `http-static`
Verifies access to static pages and big static files over http.

### `servlet-undertow`
This module covers basic scenarios about HTTP servlets under `quarkus-undertow` server more in details:
- Http session eviction
- Undertow web.xml configuration

### `jaxrs`
Simple bootstrap project created by *quarkus-maven-plugin*  
#### Additions
* *@Deprecated* annotation has been added for test regression purposes to ensure `java.lang` annotations are allowed for resources
* Resource with multipart body support, provided parts are text, image and binary data, charset checked with `us-ascii` and `utf-8`

### `reactive-routes`
This module covers some basic scenarios around reactive routes in general and also:
- Validation on request params, request body and responses.

### `vertx-web-client`
Vert.x Mutiny webClient exploratory test.
* Vert.x WebClient
* Quarkus Resteasy Mutiny / Jsonb
* Quarkus configuration converters
* Exception mapper

Also see http/vertx-web-client/README.md

### `javaee-like-getting-started`

Based on `mvn io.quarkus:quarkus-maven-plugin:999-SNAPSHOT:create     -DprojectGroupId=io.quarkus.qe     -DprojectArtifactId=scenario-101-getting-started     -DprojectVersion=1.0.0-SNAPSHOT     -DclassName="io.quarkus.qe.hello.GreetingResource"     -Dextensions=io.quarkus:quarkus-hibernate-orm,io.quarkus:quarkus-jsonb,io.quarkus:quarkus-jsonp,io.quarkus:quarkus-resteasy-jsonb,io.quarkus:quarkus-narayana-jta,io.quarkus:quarkus-elytron-security,io.quarkus:quarkus-scheduler,io.quarkus:quarkus-swagger-ui,io.quarkus:quarkus-hibernate-validator,io.quarkus:quarkus-undertow-websockets,io.quarkus:quarkus-smallrye-fault-tolerance,io.quarkus:quarkus-smallrye-metrics,io.quarkus:quarkus-smallrye-openapi,io.quarkus:quarkus-smallrye-jwt,io.quarkus:quarkus-smallrye-health,io.quarkus:quarkus-smallrye-opentracing` generated project

Uses MP health (https://quarkus.io/guides/microprofile-health) and MP metrics (https://quarkus.io/guides/microprofile-metrics).

Application:
- Define greeting resource with metrics.
- Define health checks.
- Define a bean to inject scoped HTTP beans.
- Define Fallback resource.

Tests:
- Test the health endpoints responses.
- Test greeting resource endpoint response.
- Reproducer for [QUARKUS-662](https://issues.redhat.com/browse/QUARKUS-662): "Injection of HttpSession throws UnsatisfiedResolutionException during the build phase" is covered by the test `InjectingScopedBeansResourceTest` and `NativeInjectingScopedBeansResourceIT`.
- Test to cover the functionality of the Fallback feature and ensure the associated metrics are properly updated. 

### `config`
Checks that the application can read configuration from a ConfigMap and a Secret.
The ConfigMap/Secret is exposed by mounting it into the container file system or the Kubernetes API server.

### `lifecycle-application`
Verifies lifecycle application features like `@QuarkusMain` and `@CommandLineArguments`.
Also ensures maven profile activation with properties and additional repository definition propagation into Quarkus maven plugin.

### `properties`

Module that covers the runtime configuration to ensure the changes take effect. The configuration that is covered is:
- Allow disabling/enabling `Swagger/GraphQL/Heatlh/OpenAPI` endpoints on DEV, JVM and Native modes
- Properties from YAML and external files
- Properties from Consul

### `logging/jboss`

Module that covers the logging functionality using JBoss Logging Manager. The following scenarios are covered:
- Usage of `quarkus-logging-json` extension
- Inject the `Logger` instance in beans
- Inject a `Logger` instance using a custom category
- Setting up the log level property for logger instances 

### `sql-db/sql-app`

Verifies that the application can connect to a SQL database and persist data using Hibernate ORM with Panache.
The application also uses RESTEasy to expose a RESTful API, Jackson for JSON serialization, and Hibernate Validator to validate inputs.
There are actually coverage scenarios `sql-app` directory:

- `postgresql`: the PostgreSQL JDBC driver; produces the PostgreSQL-specific build of the application and runs the OpenShift test with PostgreSQL
- `mysql`: same for MysQL
- `mariadb`: same for MariaDB
- `mssql`: same for MSSQL

All the tests deploy a SQL database directly into OpenShift, alongside the application.
This might not be recommended for production, but is good enough for test.
Container images used in the tests are:

- PostgreSQL:
  - version 10: `registry.access.redhat.com/rhscl/postgresql-10-rhel7`
  - version 12: `registry.redhat.io/rhscl/postgresql-12-rhel7` (only if `ts.redhat.registry.enabled` is set)
- MySQL:
  - version 8.0: `registry.access.redhat.com/rhscl/mysql-80-rhel7`
- MariaDB:
  - version 10.2: `registry.access.redhat.com/rhscl/mariadb-102-rhel7`
  - version 10.3: `registry.redhat.io/rhscl/mariadb-103-rhel7` (only if `ts.redhat.registry.enabled` is set)
- MSSQL: `mcr.microsoft.com/mssql/rhel/server`

### sql-db/vertx-sql
Quarkus / Vertx SQL exploratory testing. A flight search engine in order to test Quarkus Reactive SQL extensions. A detailed description can be found in sql-db/vertx-sql/README.md

### `sql-db/multiple-pus`

Verifies that the application can connect to multiple SQL databases and persist data using Hibernate ORM with Panache.
The application also uses RESTEasy to expose a RESTful API, Jackson for JSON serialization, and Hibernate Validator to validate inputs.
The multiple persistence units are defined in `application.properties`.

### `sql-db/panache-flyway`

Module that test whether we can setup a REST API using ORM Panache with Flyway and a MySQL database. Moreover, it covers XA transactions.

Topics covered here:
- https://quarkus.io/guides/hibernate-orm-panache
- https://quarkus.io/guides/rest-data-panache
- https://quarkus.io/guides/flyway

Base application:
- Define multiple datasources (https://quarkus.io/guides/datasource#multiple-datasources): default (transactions enabled), `with-xa` (transactions xa).
- Define Panache entity `ApplicationEntity` and its respective REST resource `ApplicationResource` (https://quarkus.io/guides/rest-data-panache).
- Define a REST resource `DataSourceResource` that provides info about the datasources.

Additional tests:
- Rest Data with Panache test according to https://github.com/quarkus-qe/quarkus-test-plans/blob/main/QUARKUS-976.md  
Additional UserEntity is a simple JPA entity that was created with aim to avoid inheritance of PanacheEntity methods
and instead test the additional combination of JPA entity + PanacheRepository + PanacheRepositoryResource, where
PanacheRepository is a facade class. Facade class can override certain methods to change the default behaviour of the
PanacheRepositoryResource methods.

- AgroalPoolTest, will cover how the db pool is managed in terms of IDLE-timeout, max connections and concurrency. 

### `security/basic`

Verifies the simplest way of doing authn/authz.
Authentication is HTTP `Basic`, with users/passwords/roles defined in `application.properties`.
Authorization is based on roles, restrictions are defined using common annotations (`@RolesAllowed` etc.).

### `security/jwt`

Verifies token-based authn and role-based authz.
Authentication is MicroProfile JWT, and tokens are issued manually in the test.
Authorization is based on roles, which are embedded in the token.
Restrictions are defined using common annotations (`@RolesAllowed` etc.).

### `security/keycloak`

Verifies token-based authn and role-based authz.
Authentication is OIDC, and Keycloak is used for issuing and verifying tokens.
Authorization is based on roles, which are embedded in the token.
Restrictions are defined using common annotations (`@RolesAllowed` etc.).

A simple Keycloak realm with 1 client (protected application), 2 users and 2 roles is provided in `test-realm.json`.

### `security/keycloak-authz`

Verifies token-based authn and URL-based authz.
Authentication is OIDC, and Keycloak is used for issuing and verifying tokens.
Authorization is based on URL patterns, and Keycloak is used for defining and enforcing restrictions.

A simple Keycloak realm with 1 client (protected application), 2 users, 2 roles and 2 protected resources is provided in `test-realm.json`.

### `security/keycloak-webapp`

Verifies authorization code flow and role-based authentication to protect web applications.
Authentication is OIDC, and Keycloak is used for granting user access via login form.
Authorization is based on roles, which are configured in Keycloak.
Restrictions are defined using common annotations (`@RolesAllowed` etc.).

A simple Keycloak realm with 1 client (protected application), 2 users and 2 roles is provided in `test-realm.json`.

### `security/keycloak-jwt`

Verifies authorization code flow using JWT token and role-based authentication to protect web applications.
Authentication is OIDC, and Keycloak is used for granting user access via login form.
Authorization is based on roles, which are embedded in the token.
Restrictions are defined using common annotations (`@RolesAllowed` etc.).

A simple Keycloak realm with 1 client (protected application), 2 users and 2 roles is provided in `test-realm.json`.

### `security/keycloak-oauth2`

Verifies authorization using OAuth2 protocol. Keycloak is used for issuing and verifying tokens.
Restrictions are defined using common annotations (`@RolesAllowed` etc.).

### `security/keycloak-multitenant`

Verifies that we can use a multitenant configuration using JWT, web applications and code flow authorization in different tenants. 
Authentication is OIDC, and Keycloak is used.
Authorization is based on roles, which are configured in Keycloak.

A simple Keycloak realm with 1 client (protected application), 2 users and 2 roles is provided in `test-realm.json`.

### `security/keycloak-oidc-client-basic`

Verifies authorization using `OIDC Client` extension as token generator. 
Keycloak is used for issuing and verifying tokens.
Restrictions are defined using common annotations (`@RolesAllowed` etc.).

A simple Keycloak realm with 1 client (protected application), 2 users and 2 roles is provided in `test-realm.json`.

### `security/keycloak-oidc-client-extended`

Extends the previous module `security/keycloak-oidc-client-basis` to cover reactive and RestClient integration.

Applications:
- Ping application that will invoke the Pong application using the RestClient and will return the expected "ping pong" output.
- Pong application that will return the "pong" output.
- Secured endpoints to output the claims data
- Token generator endpoint to generate tokens using the OIDC Client

Test cases:
- When calling `/ping` or `/pong` endpoints without bearer token, then it should return 401 Unauthorized. 
- When calling `/ping` or `/pong` endpoints with incorrect bearer token, then it should return 401 Unauthorized.
- When calling `/ping` endpoint with valid bearer token, then it should return 200 OK and "ping pong" as response.
- When calling `/pong` endpoint with valid bearer token, then it should return 200 OK and "pong" as response.

Variants:
- Using REST endpoints (quarkus-resteasy extension)
- Using Reactive endpoints (quarkus-resteasy-mutiny extension)
- Using Lookup authorization via `@ClientHeaderParam` annotation 
- Using `OIDC Client Filter` extension to automatically acquire the access token from Keycloak when calling to the RestClient.
- Using `OIDC Token Propagation` extension to propagate the tokens from the source REST call to the target RestClient. 

### `security/https`

Verifies that accessing an HTTPS endpoint is posible.
Uses a self-signed certificate generated during the build, so that the test is fully self-contained.

This test doesn't run on OpenShift (yet).

### `security/vertx-jwt`

In order to test Quarkus / Vertx extension security, we have set up an HTTP server with Vertx [Reactive Routes](https://quarkus.io/guides/reactive-routes#using-the-vert-x-web-router).
Basically Vertx it's an event loop that handler any kind of request as an event (Async and non-blocking). In this case the events are going to be generated by an HTTP-client, for example a browser. 
This event is going to be managed by a Router (Application.class), that based on some criteria, will dispatch these events to an existing handler. 

When a handler ends with a request, could reply a response or could propagate this request to the next handler (Handler chain approach). By this way you can segregate responsibilities between handlers. 
In our case we are going to have several handlers. 

Example:

```
this.router.get("/secured")
                .handler(CorsHandler.create("*")) 
                .handler(LoggerHandler.create()) 
                .handler(JWTAuthHandler.create(authN)) 
                .handler(authZ::authorize) 
                .handler(rc -> secure.helloWorld(rc)); 
```

* CorsHandler: add cross origin headers to the HTTP response
* LoggerHandler: log each request and response
* JWTAuthHandler: make JWT authentication task, using a given AuthN provider.
* authZ::authorize: custom AuthZ(authorization) provider.
* secure.helloWorld(rc): actual http endpoint (Rest layer).


### `microprofile`

Verifies combined usage of MicroProfile RestClient, Fault Tolerance and OpenTracing.

The test hits a "client" endpoint, which uses RestClient to invoke a "hello" endpoint.
The response is then modified in the "client" endpoint and returned back to the test.
The RestClient interface uses Fault Tolerance to guard against the "hello" endpoint errors.
It is possible to enable/disable the "hello" endpoint, which controls whether Fault Tolerance is used or not.

All HTTP endpoints and internal processing is asynchronous, so Context Propagation is also required.
JAX-RS endpoints and RestClient calls are automatically traced with OpenTracing, and some additional logging into the OpenTracing spans is also done.
Jaeger is deployed in an "all-in-one" configuration, and the OpenShift test verifies the stored traces.

### `micrometer/prometheus`

Verifies that custom metrics are exposed in the embedded Prometheus instance provided by OpenShift.

There is a PrimeNumberResource that checks whether an integer is prime or not. The application will expose the following custom metrics:
- `prime_number_max_{uniqueId}`: max prime number that was found
- `prime_number_test_{uniqueId}`: with information about the calculation of the prime number (count, max, sum)

Where `{uniqueId}` is an unique identifier that is calculated at startup time to uniquely identify the metrics of the application.

This module also covers the usage of `MeterRegistry` and `MicroProfile API`: 
                                     
- The `MeterRegistry` approach includes three scenarios: 
`simple`: single call will increment the counter.
`forloop`: will increment the counter a number of times.
`forloop parallel`: will increment the counter a number of times using a parallel flow.
- The `MicroProfile API` approach will include only the `simple` scenario.

Moreover, we also cover the `HTTP Server` metrics in order to verify the `count`, `sum` and `count` metrics work as expected.

In order to run this module, the OpenShift user must have permission to create ServiceMonitor CRDs and access to the `openshift-user-workload-monitoring` namespace. See [Other Prerequisites](#other-prerequisites) section.

### `micrometer/prometheus-kafka`

Verifies that the kafka metrics are exposed in the embedded Prometheus instance provided by OpenShift.

As part of this application, there is one Kafka consumer and one Kafka producer, therefore consumer and producer metrics are expected.

In order to run this module, the OpenShift user must have permission to create ServiceMonitor CRDs and access to the `openshift-user-workload-monitoring` namespace. See [Other Prerequisites](#other-prerequisites) section.

### `messaging/artemis`

Verifies that JMS server is up and running and Quarkus can communicate with this service.

There is a PriceProducer that pushes a new integer "price" to a JMS queue called "prices" each second.
PriceConsumer is a loop that starts at the beginning of the application runtime and blocks on reading
from the queue called "prices". Once a value is read, the attribute lastPrice is updated.
Test checks that the value gets updated.

### `messaging/artemis-jta`

Verifies that JMS server is up and running and Quarkus can communicate with this service using either transactions or client acknowledge mode.

There are three JMS queues, `custom-prices-1` and `custom-prices-2` are used to test
a transactional write: either both are correctly updated with a new value or none of them is.

`custom-prices-cack` queue is used to check that messages remains waiting in the queue until
client "acks" them, i.e. acknowledges their processing.

### `messaging/amqp-reactive`

Verifies that JMS server is up and running and Quarkus can communicate with this service.
This module is using Reactive Messaging approach by leveraging `quarkus-smallrye-reactive-messaging-amqp` extension.

There is a PriceProducer that generates message every second, the value of the message is "tick number" multiplied by 10 modulo 100.
PriceConsumer puts received number into ConcurrentLinkedQueue of Integers.
State of this queue is exposed using PriceResource which is called from the test.

### `messaging/kafka-streams-reactive-messaging`

Verifies that `Quarkus Kafka Stream` and `Quarkus SmallRye Reactive Messaging` extensions works as expected. 

There is an EventsProducer that generate login status events every 100ms. 
A Kafka stream called `WindowedLoginDeniedStream`  will aggregate these events in fixed time windows of 3 seconds. 
So if the number of wrong access excess a threshold, then a new alert event is thrown. All aggregated events(not only unauthorized) are persisted. 

- Quarkus Grateful Shutdown for Kafka connectors

This scenario covers the fix for [QUARKUS-858](https://issues.redhat.com/browse/QUARKUS-858): Avoid message loss during the graceful shutdown (SIGTERM) of the Kafka connector.
The test will confirm that no messages are lost when the `grateful-shutdown` is enabled. In the other hand, when this property is disabled, messages might be lost.

- Reactive Kafka and Kafka Streams SSL

All current tests are running under a secured Kafka by SSL. 
Kafka streams pipeline is configured by `quarkus.kafka-streams.ssl` prefix property, but reactive Kafka producer/consumer is configured by `kafka` prefix as you can see on `SslStrimziKafkaTestResource` 

### `messaging/kafka-avro-reactive-messaging`

Verifies that `Quarkus Kafka` + `Apicurio Kakfa Registry`(AVRO) and `Quarkus SmallRye Reactive Messaging` extensions work as expected. 

There is an EventsProducer that generate stock prices events every 1s. The events are typed by an AVRO schema.  
A Kafka consumer will read these events serialized by AVRO and change an `status` property to `COMPLETED`. 
The streams of completed events will be exposed through an SSE endpoint. 

### `messaging/qpid`

Verifies that JMS server is up and running and Quarkus can communicate with this service.
Similar to `messaging/artemis` scenario but using `quarkus-qpid-jms` extension to communicate with the server.

There is a PriceProducer that pushes a new integer "price" to a JMS queue called "prices" each second.
PriceConsumer is a loop that starts at the beginning of the application runtime and blocks on reading
from the queue called "prices". Once a value is read, the attribute lastPrice is updated.
Test checks that the value gets updated.

### `scaling`

An OpenShift test verifying that an OpenShift deployment with a Quarkus application scales up and down.

This test could be extended with some metric gathering.

### `external-applications`

It contains three applications:

#### `todo-demo-app`

This test produces an S2I source deployment config for OpenShift with [todo-demo-app](https://github.com/quarkusio/todo-demo-app) 
serving a simple todo checklist. The code for this application lives outside of the test suite's codebase.

The test verifies that the application with a sample of libraries is buildable and deployable via supported means.

#### `quarkus-workshop-super-heroes`

This test produces an S2I source deployment config for OpenShift with 
[Quarkus Super heroes workshop](https://github.com/quarkusio/quarkus-workshops) application.
The code for this application lives outside of the test suite's codebase.

The test verifies that the application is buildable and deployable. It also verifies that the REST and MicroProfile APIs
function properly on OpenShift, as well as the database integrations.

#### `quickstart-using-s2i`

This test mimics the quickstart tutorial provided in OpenShift.

### `scheduling/quartz`

Quartz is an open source job-scheduling framework.
We cover the following two scenarios:
- Scenario from guide: https://quarkus.io/guides/quartz
- Failover scenario where will run two instances of the same scheduled job. Then, it simulates a failover of one of the instances and then verify that the second instance continue working as expected.

### `quarkus-cli`

Verifies all the Quarkus CLI features: https://quarkus.io/version/main/guides/cli-tooling

In order to enable this module, the test suite must be executed with `-Dinclude.quarkus-cli-tests`. The Quarkus CLI is expected to be called `quarkus`. We can configure the test suite to use another Quarkus CLI binary name using `-Dts.quarkus.cli.cmd=/path/to/quarkus-dev-cli`.

### `Kamelet`

Quarkus-kamelet provide you support to interacting with Camel routes templates.
The aim of this module is to cover the following Kamelet scenarios:
* Camel producers, those scenarios where your service produces events and are consumed by a camel route
* Camel consumers, those scenarios where your service consumes a camel route
* Chain routes multiples routes
* Load application properties as routes bodies
* Validate Kamelet resources as ocp/k8s kamelet yamls(routes-temapltes, routes-bindings...)

Project folder structure

* `/resources/kamelets` contains kamelets resources as templates or KameletBindings. Also, there are groovy scripts 
in order to instantiate these templates by your self (as an example). 

* `KameletRoutes` contains templates that could be invoked (tested) directly by code. So is not 
need it to be deployed into ocp or some other platform.

### `spring/spring-data`
- Spring Data JPA: CRUD repository operation (default and custom), mapped superclass, query over embedded camelCase field, HTTP response filter.
- Spring DI: presence of Spring-defined beans in CDI context, injected transitive dependencies, multiple ways of retrieving the beans.
- Spring Data REST verifies functionality of Spring Data REST extension in following areas:
  - Automatic export of CRUD operations, manually restrict export of repo operations.
  - Usage together with Hibernate Validator constraints.
  - Pagination and sorting.
  - 1:m entity relationship.

### `spring/spring-web`
Covers two areas related to Spring Web:
- Proper behavior of SmallRye OpenAPI - correct content types in OpenAPI endpoint output (`/q/openapi`).
- Spring Boot Bootstrap application which uses Spring Web features.
  - CRUD endpoints.
  - Custom error handlers.
  - Cooperation with Qute templating engine.

### `infinispan-client`

Verifies the way of the sharing cache by Datagrid operator and Infinispan cluster and data consistency after failures.

#### Prerequisites
- Datagrid operator installed in `datagrid-operator` namespace. This needs cluster-admin rights to install.
- The operator supports only single-namespace so it has to watch another well-known namespace `datagrid-cluster`. 
This namespace must be created by "qe" user or this user must have access to it because tests are connecting to it.
- These namespaces should be prepared after the Openshift installation - See [Installing Data Grid Operator](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/running_data_grid_on_openshift/installation)

Tests create an Infinispan cluster in the `datagrid-cluster` namespace. Cluster is created before tests by `infinispan_cluster_config.yaml`. 
To allow parallel runs of tests this cluster is renamed for every test run - along with configmap `infinispan-config`. The configmap contains 
configuration property `quarkus.infinispan-client.server-list`. Value of this property is a path to the infinispan cluster from test namespace, 
its structure is `infinispan-cluster-name.datagrid-cluster-namespace.svc.cluster.local:11222`. It is because the testsuite uses dynamically generated 
namespaces for tests. So this path is needed for the tests to find Infinispan cluster in another namespace.

The Infinispan cluster needs 2 special secrets - tls-secret with TLS certificate and connect-secret with the credentials.
TLS certificate is a substitution of `secrets/signing-key` in openshift-service-ca namespace, which "qe" user cannot use (doesn't have rights on it). 
Clientcert secret is generated for "qe" from the tls-secret mentioned above.

Infinispan client tests use the cache directly with `@Inject` and `@RemoteCache`. Through the JAX-RS endpoint, we send data into the cache and retrieve it through another JAX-RS endpoint. 
The next tests are checking a simple fail-over - first client (application) fail, then Infinispan cluster (cache) fail. Tests kill first the Quarkus pod then Infinispan cluster pod and then check data.
For the Quarkus application, pod killing is used the same approach as in configmap tests. For the Infinispan cluster, pod killing is updated its YAML snipped and uploaded with zero replicas.
By default, when the Infinispan server is down and the application can't open a connection, it tries to connect again, up to 10 times (max_retries) and gives up after 60s (connect_timeout).
Because of that we are using the `hotrod-client.properties` file where are the max_retries and connect_timeout reduced. Without this the application will be still trying to connect to the Infinispan server next 10 minutes and the incremented number can appear later.
The last three tests are for testing of the multiple client access to the cache. We simulate the second client by deploying the second deployment config, Service, and Route for these tests. These are copied from the `openshift.yml` file. 

### `cache/caffeine`

Verifies the `quarkus-cache` extension using `@CacheResult`, `@CacheInvalidate`, `@CacheInvalidateAll` and `@CacheKey`.
It covers different usages: 
1. from an application scoped service
2. from a request scoped service
3. from a blocking endpoint
4. from a reactive endpoint 
