
# Quarkus Test Suite

Test Suite for Quarkus integration scenarios. Among the standard Quarkus test features, scenarios hardly rely on [Quarkus-test-framework](https://github.com/quarkus-qe/quarkus-test-framework) features

The test suite includes:
- Bare metal scenario testing
- OpenShift scenario testing
- Native testing

## How-to run Quarkus test suite

Docker, JDK 17+, and Apache Maven 3.8 are the base requirements to run the test suite using the following command:

`mvn clean verify`

Refer to [prerequisites](#Prerequisites) section for more details about the tooling.

Maven commands can be executed from the root directory or from the sub-modules. Additionally, if you want to run a single scenario you can use `-Dit.test=<TEST_CLASS_NAME>` flag.

Let's say that we want to only test `HttpMinimumIT` located over `http/http-minimum` module

```shell
cd http/http-minimum
mvn clean verify -Dit.test=HttpMinimumIT
```

In order to run a native test scenario you only need to include the native flag `-Pnative`

```shell
cd http/http-advanced
mvn clean verify -Pnative -Dit.test=HttpAdvancedIT
```

It is also possible to keep application up and running after the test execution, you can do that with `-Dts.debug -Dts.debug.run-tests` like this:

```shell
mvn clean verify -Pnative -Dit.test=HttpAdvancedIT -Dts.debug -Dts.debug.run-tests
```

You can find more information on debugging options in the [Quarkus Test Framework Wiki](https://github.com/quarkus-qe/quarkus-test-framework/wiki/1.-Getting-Started#debugging).

The following subsections will introduce how to deploy and run the test suite in OpenShift, how to filter modules, overwrite global properties or customize your native compilation

### Profiles overview

If you have a look the main `pom.xml` you will notice that there are several profiles or in other words the test suite is a maven monorepo that you can compile and verify at once or by topics. Let's review the main profiles:

* root-modules: talk about Quarkus "core stuff" as configuration or properties. Is a basic stuff that should work as a pre-requisite to other modules.
* cache-modules: cover Quarkus application data caching
* http-modules: talk about HTTP extensions and no-application endpoints like `/q/health`
* security-modules: cover all security stuff like OAuth, JWT, OpenId, Keycloak etc
* messaging-modules: is focus on brokers as Kafka or Artemis-AMQP
* monitor-modules: talk about metrics and tracing
* sql-db-modules: is focus on SQL world, Panache, Hibernate, raw SQL etc
* nosql-db-modules: is focus on noSQL or schemaless DBs as MongoDB
* spring-modules: is focus on Spring world
* quarkus-cli-tests: enable Quarkus command client test

By default, all your tests are running on bare metal (JVM / Dev mode), but you can add the following profiles to your maven command in order to activate other platforms

* Native: turn you compilation into native compilation
* OpenShift: turn your test into an openshift deployment/test
* Redhat-registry: use Redhat docker registry with official supported images instead of community images
* Serverless: enable Knative or serverless scenarios
* Operator-scenarios: enable operator scenarios, where the ecosystem of your test is going to be deployed by k8s/OCP operators

All of these profiles are not mutual exclusive, indeed we encourage you to combine these profiles in order to run complex scenarios.

**Example:**

To run in OpenShift a native version of root, security and SQL modules and also run knative scenarios of those modules

```shell
mvn clean verify -Popenshift,serverless,native,root-modules,security-modules,sql-db-modules
```
The above statement could be also re-written to the following query:

```shell
mvn clean verify -Proot-modules,security-modules,sql-db-modules -Dopenshift -Dserverless -Dnative
```
Basically, at first you specify Where and How, at second What you want to run

**NOTE:** Property `-Dall-modules` was introduced in order activate every `...-modules` profile by default. Maven activates either explicitly mentioned profiles or default profiles (explicit ones has higher priority). That is why in order to run all modules with OpenShift profile, we should also mention `-Dall-modules` or for specific set of modules appropriate `...-modules` profile.

**Example:**

```shell
mvn clean verify -Popenshift -Dall-modules
```

### OpenShift

For running the tests in OpenShift, it is expected that the user is logged into an OpenShift project:
- with OpenShift 4, run `oc login https://api.<cluster name>.<domain>:6443`

To verify that you're logged into correct project, you can run `oc whoami` and `oc project`.

By default [Quarkus-test-framework](https://github.com/quarkus-qe/quarkus-test-framework) uses [ephemeral namespaces](https://github.com/quarkus-qe/quarkus-test-framework/wiki/6.-Openshift#disable-ephemeral-namespaces) in order to be deterministic and don't populate your OCP environment with random resources. After each scenario, all the instantiated resources will be removed.

**Example:**

User: `Run http-minimum module in OpenShift.`

```shell
mvn clean verify -Dall-modules -Dopenshift -pl http/http-minimum
```

**NOTE:** here we are combining two profiles, profile `openshift` in order to trigger OpenShift execution mode and property `all-modules` to enable `http-modules` profile, where `http/http-minimum` is located.

### OpenShift & Native

Please read [OpenShift](#OpenShift) section first and login into OCP.

When we are running a [native compilation](https://quarkus.io/guides/building-native-image) the flow is the same as the regular way, the only difference is that we need to compile our application first with GraalVM/Mandrel in order to generate the binary application. To do that we will add the flag `-Dnative` to our maven command.
You have a choice of using locally installed GraalVM or a Docker base image in order to generate native executable.

#### OpenShift & Native via Docker

**Example:**

User: `Deploy in Openshift and run http-minimum module in native mode.`

```shell
mvn clean verify -Dall-modules -Dnative -Dopenshift -pl http/http-minimum
```

Quarkus test framework will reuse the Native binary generated by Maven to run the test, except if the scenario provides a build property, then it will generate a new native executable.

More info about how to generate native images could be found in Quarkus [building-native-image](https://quarkus.io/guides/building-native-image) website, and customize your native image generation by adding some [configuration flags](https://quarkus.io/guides/building-native-image#configuration-reference) to your maven command.

**Example:**

User: `Deploy in OpenShift the module http-minimum compiled with a custom GraalVM Docker image and 5GB of memory`

```shell
mvn clean verify -Dall-modules -Dnative -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 -Dquarkus.native.native-image-xmx=5g -Dopenshift -pl http/http-minimum
```

#### OpenShift & Native via local GraalVM

Is Sometimes better to use a local GraalVM in order to generate your application native executable.
Be sure that GraalVM is installed by running the following command, otherwise you will need to install it.

`gu --version`

`gu install native-image`

**Example:**

User: `Deploy in OpenShift the module http-minimum compiled with my local GraalVM in order to build my application`

```shell
mvn clean verify -Popenshift -Dall-modules -Dquarkus.package.type=native -pl http/http-minimum
```

### Bare metal

Firstly be sure that docker is running

```shell
docker run hello-world
```

**Example:**

User: `Run http-minimum module.`

```shell
mvn clean verify -Dall-modules -pl http/http-minimum
```

#### Bare metal & Native

Same as [OpenShift & Native](#OpenShift--Native) scenarios, Quarkus test framework will reuse the Native binary generated by Maven to run your tests.

**Example:**

User: `Run http-minimum module in native mode.`

```shell
mvn clean verify -Dall-modules -Dnative -pl http/http-minimum
```

All the above example for OpenShift are also valid for Bare metal, just remove the flag `-Dopenshift` and play with [native image generation properties](https://quarkus.io/guides/building-native-image#configuration-reference)

### Additional notes

Have a look at the main `pom.xml` file and pay attention to some useful areas as how the scenarios are categorized by topics/profiles, or some global properties as `quarkus.platform.version` that could be overwritten by a flag.

**Example:**

As a user I would like to run all core modules of Quarkus `2.2.3.Final`

```shell
mvn clean verify -Droot-modules -Dquarkus.platform.version=2.2.3.Final
```

Since this is standard Quarkus configuration, it's possible to override using a system property.
Therefore, if you want to run the tests with a different Java S2I image, run `mvn clean verify -Dquarkus.s2i.base-jvm-image=...`.

### Prerequisites

In addition to common prerequisites for Java projects (JDK, GraalVM, Maven) and OpenShift (`oc`), the following utilities must also be installed on the machine where the test suite is being executed:

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

These requirements are necessary to verify the tests using Operators.

### Groups overview
The suite uses `org.junit.jupiter.api.Tag` annotation to group similar tests together. They can be enabled/disabled with standard Maven options `groups` and `excludedGroups` (see https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html). Aside from specific use-cases (eg `@Tag("QUARKUS-1547")`), there are several common groups used through the test suite:
- `fips-incompatible`: tests, which broke when running on FIPS 140-2 compliant hosts
- `use-quarkus-openshift-extension`: tests use `quarkus-openshift` extension to deploy the app
- `serverless`: use Openshift Serverless to deploy the app
- `quarkus-cli`: tests use Quarkus CLI, which needs to be installed ( see https://quarkus.io/guides/cli-tooling for details)
- `podman-incompatible`: tests, which require Docker as a container engine and are not compatible with Podman.

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
  - `2.7` for Red Hat build of Quarkus 2.7.z (corresponding upstream version: `2.7.7.Final`)
  - `2.13` for Red Hat build of Quarkus 2.13.z (corresponding upstream version: `2.13.7.Final`)

When creating new branch please ensure following items:
 - There are no pending PRs for relevant RHBQ or Quarkus stream
 - Pin external application branches, see for example https://github.com/quarkus-qe/quarkus-test-suite/pull/1478
 - Update GH Actions to use the right Quarkus version, see for example https://github.com/quarkus-qe/quarkus-test-suite/commit/cc55ea28e29b1a5a6c29824dfac2bfe04637c354 (and minor fix https://github.com/quarkus-qe/quarkus-test-suite/commit/b37eabfa7a6b3e96fc119d89111d305a0a82df5e)
 - Ensure https://github.com/quarkus-qe/quarkus-test-suite/blob/main/lifecycle-application/pom.xml#L33 uses the right version for relevant branch

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
./mvnw clean verify -Dall-modules -Dinclude.serverless
```

### OpenShift Operators

The test suite contains a Maven profile activated using the `include.operator-scenarios` property or `operator-scenarios` profile name.
This profile includes additional modules with serverless test coverage into the execution of the testsuite.
Serverless test coverage supports both JVM and Native mode.

The following command will execute the whole test suite including serverless tests:

```
./mvnw clean verify -Dall-modules -Dinclude.operator-scenarios
```

## Existing tests

### `http/http-minimum`
Verifies that you can deploy a simple HTTP endpoint to OpenShift and access it.
It also verifies multiple deployment strategies like:
- Serverless
- Using OpenShift quarkus extension
- Using OpenShift quarkus extension and Docker Build strategy

### `http/http-minimum-reactive`
Reactive equivalent of the http/http-minimum module

### `http/http-advanced`
Verifies Server/Client http_2/1.1, Grpc and http redirections and Jakarta REST provider field injection.

### `http/http-advanced-reactive`
Reactive equivalent of the http/http-advanced module

### `http/http-static`
Verifies access to static pages and big static files over http.

### `http/servlet-undertow`
This module covers basic scenarios about HTTP servlets under `quarkus-undertow` server more in details:
- Http session eviction
- Undertow web.xml configuration

### `http/jakarta-rest`
Simple bootstrap project created by *quarkus-maven-plugin*

### `http/jakarta-rest-reactive`
RESTEasy Reactive equivalent of `http/jakarta-rest`. Tests simple and multipart endpoints.
Additional coverage:
- Execution model (blocking vs. non-blocking) of endpoints based on method signature.
- HTTP Caching features.
- Advanced JSON serialization.
- Gzip compression
- REST Client reactive - support for POJO JSON serialization in multipart forms.
- Request matching - selecting from multiple media types
- Handling Multipart Form data

### `http/rest-client`
Verifies Rest Client configuration using `quarkus-resteasy-client-jaxb` (XML support) and `quarkus-resteasy-client-jsonb` (JSON support).
This module will setup a very minimal configuration (only `quarkus-resteasy`) and have four endpoints:
- Two endpoints to get a book in JSON and XML formats.
- Two endpoints to get the value of the previous endpoints using the rest client interface.

### `http/rest-client-reactive`
Reactive equivalent of the http/rest-client module.
Exclusions: XML test. Reason: https://quarkus.io/blog/resteasy-reactive/#what-jax-rs-features-are-missing

Additionally, it's include endpoints to check usage of HTTP/2 using REST client over the http and https.
HTTP/2 REST client tests check sync and async response by:
- Two endpoint used by client
- Eight endpoints used for validation of client request


### `http/rest-client-reactive-vanilla`
Verifies Rest Client usage, while no request are going only internally on the server.
This module requires to not have any resteasy dependency, for an issue to be reproducible. 

### `http/hibernate-validator`
Verifies HTTP endpoints validation using `quarkus-hibernate-validator` works correctly in Resteasy Classic and Resteasy Reactive.
This module will setup a simple endpoint and will validate the right message format is set when there are validation errors.

### `http/vertx`
Verifies that you can deploy a simple Vert.X-based HTTP endpoint to OpenShift, access it and retrieve metrics for it.
It also verifies multiple deployment strategies like:
- Serverless
- Using OpenShift quarkus extension
- Using OpenShift quarkus extension and Docker Build strategy

### `http/management`
Verifies, that management interface (micrometer metrics and health endpoints) can be hosted on a separate port

#### Additions
* *@Deprecated* annotation has been added for test regression purposes to ensure `java.lang` annotations are allowed for resources
* Resource with multipart body support, provided parts are text, image and binary data, charset checked with `us-ascii` and `utf-8`

### `http/reactive-routes`
This module covers some basic scenarios around reactive routes in general and also:
- Validation on request params, request body and responses.
- Gzip compression

### `http/vertx-web-client`
Vert.x Mutiny webClient exploratory test.
* Vert.x WebClient
* Quarkus Resteasy Mutiny / Jsonb
* Quarkus configuration converters
* Exception mapper

Also see http/vertx-web-client/README.md

### `http/vertx-web-validation`
Ensure that you can deploy a simple Quarkus application with predefined routes using schema parser from vertx-json-schema and Router Vert.x approach,
 incorporating web validation configuration through ValidationHandlerBuilder. One of the goal of vertx-web-validation functionality is to validate parameters and bodies of incoming requests.

It also verifies multiple deployment strategies like:
- Using Quarkus OpenShift extension

### `http/graphql`
This module covers some basic scenarios around GraphQL.

### `http/graphql-telemetry`
This module covers some basic scenarios around OpenTelemetry usage with GraphQL.

### `helm/helm-minimum`
This module covers Helm integration by Quarkus-helm extension`io.quarkiverse.helm:quarkus-helm`
The above extension will generate a Helm chart based on your `application.properties`. We have set up
a HelloWorld application and then deploy it into Openshift through Helm.

*Note:* This module will push a docker image into `quay.io/quarkusqeteam/ts-quarkus-helm-simple-app` repository, you will need to be logged into Quay in order to push the image.

### `javaee-like-getting-started`

Based on `mvn io.quarkus:quarkus-maven-plugin:999-SNAPSHOT:create     -DprojectGroupId=io.quarkus.qe     -DprojectArtifactId=scenario-101-getting-started     -DprojectVersion=1.0.0-SNAPSHOT     -DclassName="io.quarkus.qe.hello.GreetingResource"     -Dextensions=io.quarkus:quarkus-hibernate-orm,io.quarkus:quarkus-jsonb,io.quarkus:quarkus-jsonp,io.quarkus:quarkus-resteasy-jsonb,io.quarkus:quarkus-narayana-jta,io.quarkus:quarkus-elytron-security,io.quarkus:quarkus-scheduler,io.quarkus:quarkus-swagger-ui,io.quarkus:quarkus-hibernate-validator,io.quarkus:quarkus-undertow-websockets,io.quarkus:quarkus-smallrye-fault-tolerance,io.quarkus:quarkus-smallrye-metrics,io.quarkus:quarkus-smallrye-openapi,io.quarkus:quarkus-smallrye-jwt,io.quarkus:quarkus-smallrye-health,io.quarkus:quarkus-opentelemetry` generated project.
MapStruct dependency was added manually.

Uses MP health (https://quarkus.io/guides/microprofile-health) and MP metrics (https://quarkus.io/guides/microprofile-metrics).

Application:
- Define greeting resource with metrics.
- Define health checks.
- Define a bean to inject scoped HTTP beans.
- Define Fallback resource.
- Define Java bean mappings resource with responses mapped from POJOs to DTOs.

Tests:
- Test the health endpoints responses.
- Test greeting resource endpoint response.
- Reproducer for [QUARKUS-662](https://issues.redhat.com/browse/QUARKUS-662): "Injection of HttpSession throws UnsatisfiedResolutionException during the build phase" is covered by the test `InjectingScopedBeansResourceTest` and `NativeInjectingScopedBeansResourceIT`.
- Test to cover the functionality of the Fallback feature and ensure the associated metrics are properly updated.
- Test MapStruct with Quarkus in JVM and native mode.

### `config`
Checks that the application can read configuration from a ConfigMap and a Secret.
The ConfigMap/Secret is exposed by mounting it into the container file system or the Kubernetes API server.
Also ensures proper integration of SmallRye Config [Secret Keys](https://smallrye.io/smallrye-config/Main/config/secret-keys/) with Quarkus.

### `docker-build`
Checks that an application can generate a Docker image based on some configuration parameters.

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
- Check default `quarkus.log.min-level` value

### `sql-db/hibernate`

This module contains Hibernate integration scenarios.

The features covered:
* Reproducer for [14201](https://github.com/quarkusio/quarkus/issues/14201) and
  [14881](https://github.com/quarkusio/quarkus/issues/14881): possible data loss bug in hibernate. This is covered under
  the Java package `io.quarkus.qe.hibernate.items`.
- Reproducer for [QUARKUS-661](https://issues.redhat.com/browse/QUARKUS-661): `@TransactionScoped` Context does not call
  `@Predestroy` on `TransactionScoped` beans. This is covered under the Java package `io.quarkus.qe.hibernate.transaction`.

### `sql-db/hibernate-fulltext-search`

This module covers `elasticSearch` fulltext look up, over a `Hibernate` multi-tenant scenario.
Feature covered:
* Hibernate (no panache) multitenant scenario
* Elastic Fulltext Search verification
  * Sorted result test
  * case insensitive test
* PostgresSQL + native + Openshift
* Mysql + native + Openshift

### `sql-db/sql-app`

Verifies that the application can connect to a SQL database and persist data using Hibernate ORM with Panache.
The application also uses RESTEasy to expose a RESTful API, Jackson for JSON serialization, and Hibernate Validator to validate inputs.
There are actually coverage scenarios `sql-app` directory:

- `postgresql`: the PostgreSQL JDBC driver; produces the PostgreSQL-specific build of the application and runs the OpenShift test with PostgreSQL
- `mysql`: same for MySQL
- `mariadb`: same for MariaDB
- `mssql`: same for MSSQL
- `oracle`: The same case as the others, but for Oracle, only JVM mode is supported. Native mode is not covered due to a bug in Quarkus, which causes it to fail when used in combination with other JDBC drivers (see `OracleDatabaseIT`). OpenShift scenario is also not supported due to another bug (see `OpenShiftOracleDatabaseIT`).

All the tests deploy an SQL database directly into OpenShift, alongside the application.
This might not be recommended for production, but is good enough for test.
Container images used in the tests are:

- PostgreSQL:
  - version 16: `postgres:16.1`
  - version 10: `registry.redhat.io/rhel8/postgresql-10` (only if `ts.redhat.registry.enabled` is set)
- MySQL:
  - version 5.7: `mysql:5.7`
  - version 8.0: `registry.access.redhat.com/rhscl/mysql-80-rhel7`
- MariaDB:
  - version 10.11: `mariadb:10.11`
  - version 10.3: `registry.redhat.io/rhel8/mariadb-103`
  - version 10.5: `registry.redhat.io/rhel8/mariadb-105`
- MSSQL: `mcr.microsoft.com/mssql/rhel/server`
- Oracle
  - version 23: `gvenzl/oracle-free:23-slim-faststart`

### `sql-db/sql-app-oracle`
Functionally identical to `sql-db/sql-app`, but using only `quarkus-jdbc-oracle` driver. This is a workaround for the missing native Oracle coverage in `sql-db/sql-app`.


### `sql-db/hibernate-reactive`
Verifies that the application can work with data persisted in SQL database in reactive manner. Basically, the same as `sql-app`, but reactive.
Covered DBs:
- Postgres
- MySQL
- DB2
- MSSQL
- Oracle

### `sql-db/vertx-sql`
Quarkus / Vertx SQL exploratory testing. A flight search engine in order to test Quarkus Reactive SQL extensions. A detailed description can be found in sql-db/vertx-sql/README.md

### `sql-db/reactive-vanilla`
Verifies `quarkus-reactive-pg-client` extension and DevServices integration
Verifies `quarkus-reactive-mysql-client` extension and DevServices integration
Verifies `quarkus-reactive-mssql-client` extension and DevServices integration

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
Additional UserEntity is a simple Jakarta Persistence entity that was created with aim to avoid inheritance of PanacheEntity methods
and instead test the additional combination of Jakarta Persistence entity + PanacheRepository + PanacheRepositoryResource, where
PanacheRepository is a facade class. Facade class can override certain methods to change the default behaviour of the
PanacheRepositoryResource methods.

- AgroalPoolTest, will cover how the db pool is managed in terms of IDLE-timeout, max connections and concurrency.

### `sql-db/reactive-rest-data-panache`

This is a subset of `sql-db/panache-flyway` module modified to use RESTEasy Reactive instead of RESTEasy Classic.

It focuses on single topic: https://quarkus.io/guides/rest-data-panache.

Base application is reduced to two REST resources:
- `ApplicationResource`, which extends `PanacheEntityResource`, and its Panache entities: `ApplicationEntity`, `VersionEntity`, `ServiceEntity`.
- `UserResource`, which extends `PanacheRepositoryResource`, a Panache repository facade `UserRepository` and related Panache entity `UserEntity`.

Tests cover the supported functionality of `rest-data-panache`: CRUD operations, `json` and `hal+json` data types,
invalid input, filtering, sorting, pagination.

### `sql-db/narayana-transactions`

Verifies Quarkus transaction programmatic API, JDBC object store and transaction recovery.
Base application contains REST resource `TransferResource` and three main services: `TransferTransactionService`, `TransferWithdrawalService`
and `TransferTopUpService` which implement various bank transactions. The main scenario is implemented in `TransactionGeneralUsageIT`
and checks whether transactions and rollbacks always done in full.

OpenTelemetry JDBC instrumentation test coverage is also placed here. JDBC tracing is tested for all supported
databases in JVM mode, native mode and OpenShift. Smoke tests for DEV mode are using PostgreSQL. Smallrye Context Propagation
cooperation with OpenTelemetry in DEV mode is also placed in this module.

### `security/basic`

Verifies the simplest way of doing authn/authz.
Authentication is HTTP `Basic`, with users/passwords/roles defined in `application.properties`.
Authorization is based on roles, restrictions are defined using common annotations (`@RolesAllowed` etc.).

### `security/bouncycastle-fips`

Verify `bouncy castle FIPS` integration with Quarkus-security.
Bouncy castle providers:
- BCFIPS
- BCFIPSJSSE

### `security/form-authn`

Verifies form-based authentication.
Verifies that Basic Authentication is not used as a fallback option when it is explicitly disabled.

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

### `security/keycloak-authz-classic`

Verifies token-based authn and URL-based authz.
Authentication is OIDC, and Keycloak is used for issuing and verifying tokens.
Authorization is based on URL patterns, and Keycloak is used for defining and enforcing restrictions.

A simple Keycloak realm with 1 client (protected application), 2 users, 2 roles and 2 protected resources is provided in `test-realm.json`.

### `security/keycloak-authz-reactive`
QUARKUS-1257 - Verifies authenticated endpoints with a generic body in parent class
Verifies token-based authn and URL-based authz.
Authentication is OIDC, and Keycloak is used for issuing and verifying tokens.
Authorization is based on URL patterns, and Keycloak is used for defining and enforcing restrictions.

A simple Keycloak realm with 1 client (protected application), 2 users, 2 roles and 3 protected resources is provided in `test-realm.json`.

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
- OIDC logout flow

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

### `security/keycloak-oidc-client-reactive`

Verifies special cases of using reactive OIDC client:
- Proper handling of `Authorization` request header by `OidcClientRequestReactiveFilter`: the filter should always add a single`Authorization` header, not duplicate it in multiple request attempts.

### `security/keycloak-oidc-client-reactive-extended`

Reactive twin of the `security/keycloak-oidc-client-extended`, extends `security/keycloak-oidc-client-reactive-basic` and also covers some special cases that are common for both classic and reactive modules:

- Verifies Proof Of Key for Code Exchange support for a Keycloak and Red Hat Single Sign-On together with OIDC Single Page Application logout flow

### `securty/oidc-client-mutual-tls`

Verifies OIDC client can be authenticated as part of the `Mutual TLS` (`mTLS`) authentication process
when OpenID Connect Providers requires so. Keycloak is used as a primary OIDC server and Red Hat SSO
is used for OpenShift scenarios.

Test cases:
- Authentication works when user has correct certificates
- Authentication does not work when user doesn't use any certificate
- Application endpoint require authentication (fail test)

Variants:
- Using JKS Keystore type (autodetected based on a keystore file extension)
- Using PKCS #12 Keystore type (autodetected based on a keystore file extension)
- Using PKCS #12 Keystore type with unknown file extension (Keystore type declared via configuration properties)
- Actual Keystore type and declared keystore type mismatch leads to an authentication failure
- Running on OpenShift with a stable Red Hat SSO
- Running on OpenShift with the latest Red Hat SSO

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

### `security/webauthn`
Verifies WebAuthn authentication mechanism.

To test WebAuthn we have set up a reactive Mysql database container.

However,testing WebAuthn can be challenging because it typically requires a hardware token. To address this,
we've created 'MyWebAuthnHardware' class to simulate that hardware token.

Additionally, we utilize some webauthn enpoints such as "/q/webauthn/register" and "/q/webauthn/callback".

Restrictions are defined using role-based access control (RBAC) annotations such as `@RolesAllowed`.

Test cases

* Check the quarkus application, accessing different endpoints without any registered user.
* Try to register a user with webauthn.
* Try to register the same user name.
* Try to login as the registered user.
* Try to simulate a register user without the specific webauthn data required (challenge, public key credentials, type, rawId, etc).
* Check for a failed login attemp with an improperly registered user.


### service-binding/postgresql-crunchy-classic and service-binding/postgresql-crunchy-reactive

Modules verifying Quarkus `kubernetes-service-binding` extension is able to inject application projection service
binding from a PostgreSQL cluster created by Crunchy Postgres operator.
Binding is verified for both classic and reactive SQL clients (`quarkus-jdbc-postgresql` and `quarkus-reactive-pg-client`).

The module requires a cluster with Kubernetes API >=1.21 to work with Red Hat Service Binding Operator and Crunchy
Postgres v5 (this means OCP 4.7 and upwards.)

This module requires an installed Crunchy Postgres Operator v5 and Red Hat Service Binding Operator.

`OpenShiftPostgreSqlSbIT` creates a `PostgresCluster` and deploys a simple TODO application to verify that the service
binding is injected by making a request at its declared endpoint. Analogous test is done by `OpenShiftPostgreSqlReactiveSbIT` to verify a reactive way.

### Service-discovery/stork

Verifies Stork integration in order to provide service discovering and round-robin load balancing between services

`StorkServiceDiscoveryIT` scenario deploys four services:
* Pung: is a simple endpoint that returns "pung" as a string
* Pong: is a simple endpoint that returns "pong" as a string
* PongReplica: is a "Pong service" replica, that is deployed in another physical service
* Ping: is the main client microservice that will use `pung` and `pong` (Pong and PongReplica) services. The service
discovery will be done by Stork, and the request dispatching between "pong" services is going to be done by Stork load balancer.

### Service-discovery/stork-custom

Verify Stork custom service discovery and load balancer implementation.
`StorkCustomLoadBalancerIT` scenario deploys three services:
* Pong: is a simple endpoint that returns "pong" as a string
* PongReplica: is a "Pong service" replica, that is deployed in another physical service
* Ping: is the main client microservice that will use `pong` and `pong-replica` services.

### `monitoring/microprofile`

Verifies combined usage of MicroProfile RestClient, Fault Tolerance and OpenTracing.

The test hits a "client" endpoint, which uses RestClient to invoke a "hello" endpoint.
The response is then modified in the "client" endpoint and returned back to the test.
The RestClient interface uses Fault Tolerance to guard against the "hello" endpoint errors.
It is possible to enable/disable the "hello" endpoint, which controls whether Fault Tolerance is used or not.

All HTTP endpoints and internal processing is asynchronous, so Context Propagation is also required.
Jakarta REST endpoints and RestClient calls are automatically traced with OpenTracing, and some additional logging into the OpenTracing spans is also done.
Jaeger is deployed in an "all-in-one" configuration, and the OpenShift test verifies the stored traces.

### `monitoring/opentelemetry`

Testing OpenTelemetry with Jaeger components
 - Extension `quarkus-opentelemetry` - responsible for traces generation in OpenTelemetry format and export into OpenTelemetry components (opentelemetry-agent, opentelemetry-collector)

Scenarios that test proper traces export to Jaeger components, context propagation, OpenTelemetry SDK Autoconfiguration and CDI injection of OpenTelemetry beans.
See also `monitoring/opentelemetry/README.md`

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

### `messaging/infinispan-grpc-kafka`

Verifies that gRPC, Infinispan and Kafka extensions work together with SASL authentication.

### `messaging/kafkaSSL`
Verifies KafkaSSL integration. This module cover a simple Kafka producer/consumer through SSL

### `messaging/kafka-streams-reactive-messaging`

Verifies that `Quarkus Kafka Stream` and `Quarkus SmallRye Reactive Messaging` extensions works as expected.

There is an EventsProducer that generate login status events every 100ms.
A Kafka stream called `WindowedLoginDeniedStream`  will aggregate these events in fixed time windows of 3 seconds.
So if the number of wrong access excess a threshold, then a new alert event is thrown. All aggregated events(not only unauthorized) are persisted.

- Quarkus Grateful Shutdown for Kafka connectors

This scenario covers the fix for [QUARKUS-858](https://issues.redhat.com/browse/QUARKUS-858): Avoid message loss during the graceful shutdown (SIGTERM) of the Kafka connector.
The test will confirm that no messages are lost when the `grateful-shutdown` is enabled. In the other hand, when this property is disabled, messages might be lost.

- Reactive Kafka and Kafka Streams SSL
- Auto-detect serializers and deserializers for the Reactive Messaging Kafka Connector

All current tests are running under a secured Kafka by SSL.
Kafka streams pipeline is configured by `quarkus.kafka-streams.ssl` prefix property, but reactive Kafka producer/consumer is configured by `kafka` prefix as you can see on `SslStrimziKafkaTestResource`

### `messaging/kafka-confluent-avro-reactive-messaging`

- Verifies that `Quarkus Kafka` + `Apicurio Kakfa Registry`(AVRO) and `Quarkus SmallRye Reactive Messaging` extensions work as expected.

There is an EventsProducer that generate stock prices events every 1s. The events are typed by an AVRO schema.
A Kafka consumer will read these events serialized by AVRO and change an `status` property to `COMPLETED`.
The streams of completed events will be exposed through an SSE endpoint.

### `messaging/kafka-strimzi-avro-reactive-messaging`

- Verifies that `Quarkus Kafka` + `Apicurio Kakfa Registry`(AVRO) and `Quarkus SmallRye Reactive Messaging` extensions work as expected.

There is an EventsProducer that generate stock prices events every 1s. The events are typed by an AVRO schema.
A Kafka consumer will read these events serialized by AVRO and change an `status` property to `COMPLETED`.
The streams of completed events will be exposed through an SSE endpoint.

- Verify random kafka group id
- RedPanda/Apicurio devServices (Kafka + AVRO)

### `messaging/kafka-producer`

This scenario is focus on issues related only to Kafka producer.
Verifies that Kafka producer doesn't block the main thread  and also doesn't takes more time than `mp.messaging.outgoing.<CHANNEL>.max.block.ms`, and also
doesn't retry more times than `mp.messaging.outgoing.<CHANNEL>.retries`

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

External applications need a base image which is used by OpenShift to build the app on. So, one base image needs to be supplied forJVM and Native:
- For JVM: `quarkus.s2i.base-jvm-image`
- For Native: `quarkus.s2i.base-native-image`

These properties have default values set in the pom.xml file.

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

### `scheduling/spring`

We cover the following scenario from guide: https://quarkus.io/guides/spring-scheduled with some adjustments to use only Spring API.

### `quarkus-cli`

Verifies all the Quarkus CLI features: https://quarkus.io/version/main/guides/cli-tooling

In order to enable this module, the test suite must be executed with `-Dall-modules -Dinclude.quarkus-cli-tests`. The Quarkus CLI is expected to be called `quarkus`. We can configure the test suite to use another Quarkus CLI binary name using `-Dts.quarkus.cli.cmd=/path/to/quarkus-dev-cli`.

#### Windows Prerequisites
Ensure UTF-8 support is enabled, see for example https://stackoverflow.com/questions/57131654/using-utf-8-encoding-chcp-65001-in-command-prompt-windows-powershell-window or search for `chcp 65001`. `QuarkusCliCreateJvmApplicationIT.shouldCreateApplicationWithCodeStarter` and `QuarkusCliCreateJvmApplicationIT.shouldCreateJacocoReportsFromApplicationOnJvm` tests fail without UTF-8 support enabled on Windows.

### `Qute`
Coverage for Qute template engine.
Module `qute/synchronous` contains coverage for Qute templating and integration with RESTEasy.
Module `qute/reactive` contains coverage for Qute templating and integration with RESTEasy reactive.
Module `qute/multimodule` provides coverage for complicated issue of having localised messages in separate modules.

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

  ### `spring/spring-web-reactive`
  Covers two areas related to Spring Web Reactive:
  - Proper behavior of SmallRye OpenAPI with Mutiny method signatures - correct content types in OpenAPI endpoint output (`/q/openapi`).
  - Spring Boot Bootstrap application which uses Spring Web Reactive features.
    - CRUD endpoints.
    - Custom error handlers.
    - Cooperation with Qute templating engine.
    - Verify functionality of methods with transactional annotation @ReactiveTransactional
    - Verify functionality of methods with transactional method (.withTransactional)

### `spring/spring-cloud-config`

Verifies that we can use an external Spring Cloud Server to inject configuration in our Quarkus applications.

### `spring/spring-properties`
Exploratory testing for the `quarkus-spring-boot-properties` Quarkus extension. The application consists of a REST endpoint
with some different approaches to inject properties.

Current limitations:
- Relaxing name convention is not supported and it won't be supported: https://github.com/quarkusio/quarkus/issues/12483
- The annotation `@ConstructorBinding` is not supported yet: https://github.com/quarkusio/quarkus/issues/19364

### `infinispan-client`

Verifies the way of the sharing cache by Datagrid operator and Infinispan cluster and data consistency after failures.
Verifies cache entries serialization, querying and cache eviction.

#### Prerequisites
- Datagrid operator installed in `datagrid-operator` namespace. This needs cluster-admin rights to install.
- The operator supports only single-namespace so it has to watch another well-known namespace `datagrid-cluster`.
This namespace must be created by "qe" user or this user must have access to it because tests are connecting to it.
- These namespaces should be prepared after the Openshift installation - See [Installing Data Grid Operator](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/running_data_grid_on_openshift/installation)

Tests create an Infinispan cluster in the `datagrid-cluster` namespace. Cluster is created before tests by `infinispan_cluster_config.yaml`.
To allow parallel runs of tests this cluster is renamed for every test run - along with configmap `infinispan-config`. The configmap contains
configuration property `quarkus.infinispan-client.hosts`. Value of this property is a path to the infinispan cluster from test namespace,
its structure is `infinispan-cluster-name.datagrid-cluster-namespace.svc.cluster.local:11222`. It is because the testsuite uses dynamically generated
namespaces for tests. So this path is needed for the tests to find Infinispan cluster in another namespace.

The Infinispan cluster needs 2 special secrets - tls-secret with TLS certificate and connect-secret with the credentials.
TLS certificate is a substitution of `secrets/signing-key` in openshift-service-ca namespace, which "qe" user cannot use (doesn't have rights on it).
Clientcert secret is generated for "qe" from the tls-secret mentioned above.

Infinispan client tests use the cache directly with `@Inject` and `@RemoteCache`. Through the Jakarta REST endpoint, we send data into the cache and retrieve it through another Jakarta REST endpoint.
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

### `cache/redis`

Verifies the `quarkus-redis-cache` extension using `@CacheResult`, `@CacheInvalidate`, `@CacheInvalidateAll` and `@CacheKey`.
It covers different usages:
1. from an application scoped service
2. from a request scoped service

Also verify that Qute correctly indicate that does not work with remote cache.

### `cache/spring`

Verifies the `quarkus-spring-cache` extension using `@Cacheable`, `@CacheEvict` and `@CachePut`.
It covers different usages:
1. from an application scoped service
2. from a request scoped service
3. from a REST controller endpoint (using `@RestController)

More information about this extension in https://quarkus.io/guides/spring-cache.

### `test-tooling/pact`

Verifies, that quarkus works correctly with third-party tool called Pact-JVM

### `nosql-db/mongodb`

Test data operations on MongoDB: insert one document into a collection, list all documents in a collection, find documents from
a collection using a filter and a projection. All tests are performed using:

- MongoClient
- MongoClient with BSON codec for all entities

### `nosql-db/mongodb-reactive`

Reactive equivalent of `nosql-db/mongodb`. Uses reactive ReactiveMongoClient (without codecs)

### `nosql-db/infinispan`

Provides some coverage similar to `infinispan-client` module, but with focus on connecting to local cluster and Dev mode and not on working with OpenShift

### `nosql-db/elasticsearch`

Provides coverage for quarkus-elasticsearch-rest-client

### `websockets/quarkus-websockets`
Coverage for sending messages over websockets

### `websockets/websockets-client`
Coverage for sending messages over websockets with only a client library

### `funqy/knative-events`

Verifies [Quarkus Funqy Knative Events](https://quarkus.io/guides/funqy-knative-events) deployed to OpenShift and unit testing using RestAssured works according to the documentation.

#### Prerequisites
- OpenShift Serverless Functions installed.
- Knative Eventing installed.
- Logged user must have a cluster role with all permissions to `knativeeventings` resource in the `operator.knative.dev` API group.

Test coverage includes:
- Quarkus function deployment using Quarkus OpenShift extension
- Quarkus function receives and processes:
  - bean (f.e. POJO)
  - `CloudEvent` using the Funqy `@Context` annotation
  - binary data
  - primitive type or its wrapper
- Reading and writing of the attributes of a `CloudEvent`
- Permitted return types:
  - void
  - String
  - byte[]
  - primitive type and its wrapper
  - maps
  - `CloudEvents<E>`
  - `Uni<T>`, where `T` is any of types listed above
- Quarkus function accepts events from custom resource created by us as well as from Event Sources provided out of the box
- Cloud Event type and source matching:
  - default mapping when the type match the function name for the function to trigger
  - mapping changed by configuration within application.properties
  - mapping with `@CloudEventMapping` annotation
- Complex function chain, when one function triggers the next and so on
- Unit testing using RestAssured using:
  - normal HTTP requests
  - Cloud Event Binary mode
  - Structured Mode
- Multiple functions declared in one application

### `build-time-analytics`

Verifies the Quarkus build-time analytics feature: https://quarkus.io/guides/build-analytics.

For details, see the module-specific README.md.
