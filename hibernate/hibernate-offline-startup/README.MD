# Hibernate Offline Startup

Tests are motivated by the [QUARKUS-3363](https://issues.redhat.com/browse/QUARKUS-3363) and planned in the [test plan](https://github.com/quarkus-qe/quarkus-test-plans/blob/main/QUARKUS-3363.MD).

## Challenges

* Hibernate multitenancy types:
    * MariaDB and MySQL are testing the database-based multitenancy since they don’t support the schema.
    * SQL Server is also testing the database-based multitenancy, even though it does support schema, the default schema is “dbo”, and when Hibernate attempts to change the schema, the driver reports that as an unsupported action. It should be possible to switch schemes by changing a user's credentials with their own default schema, but I couldn’t make it work.
    * PostgreSQL is testing schema-based multitenancy.
    * Oracle supports schemes; however, configuring Oracle to create schemas is challenging. Granting users admin rights or every possible right to the individual schema passed; however, when Hibernate succeeded in creating the schema and tried to create sequences in the schema, it always failed on permissions. I think we can’t do both: ask Hibernate to create the schema while also preparing schema permissions beforehand. Therefore, we leverage the fact that the Oracle image supports creating multiple databases and use individual schemas named after the user.
* Credentials propagation; in our customer case, database credentials were obtained dynamically from incoming requests after the database started; also, Quarkus documentation mentions a scenario where the database pod is not available during the Quarkus application startup. We have disabled pooling so that every database query means a new connection that requires credentials and testing:
    * ability to send credentials before any database requests (using Hibernate tenant resolver and credentials provider)
    * ability to send credentials and with the same request query database (using Hibernate tenant resolver and credentials provider)
    * ability to create a connection “manually” while accessing credentials from the incoming HTTP request
* Fixed ports are required so that we can start Quarkus with a JDBC URL that is not available:
    * OCP pods expose the same ports as we specify in the @Container annotation; therefore, we can use a hardcoded internal service URL with a fixed port
    * Bare-metal tests use Test Containers, which try to discourage fixed ports (the setter method is protected) due to the fact that they can cause issues with the remote Docker host; we should not introduce this feature to our framework. Therefore, a new resource manager is created in this module. On Windows, the remote Docker host is ported to the localhost so that hardcoded JDBC URLs work.
    * Nginx would allow us to start the database with a dynamic port and redirect it to the port we specify in the Quarkus configuration. I have tried Nginx to only permit traffic once we need it, but its resolver expects to resolve network aliases to their IPs on startup. I didn’t find a configuration that worked for both Docker and Podman. It also required more code for provisioning test containers with the shared network.
* DB2 and privileged mode
    * In other test modules, we configure the privileged mode for DB2 in the scope of the DB2 service (AKA field is called db2, hence we only configure it for the service called db2). However, we look up the database by a service name in the test parent, so I could not use a different name for the DB2 service. Hence, the privileged mode is configured when the DB2 image is recognized instead.
* OpenTelemetry JDBC Instrumentation
    * We create a schema using the Hibernate ORM SchemaManager, which is not supported for multitenancy (but works like a charm...). However, if we disable telemetry for the req_scope_credentials datasource and keep it for the app_scope_credentials datasource while both datasources are used by the same Hibernate persistence unit, traces for both schemes generation are present. I have tried to create a reproducer with supported scenarios and couldn’t. Therefore, we test telemetry for all tenants (datasources) resolved for the default persistence unit.
* Detection of build-time configuration properties for forced dependencies
    * Current mechanism used by our framework to detect build-time properties relies on a classpath during the testing framework bootstrap. Thus, we either need to hardcode a list of build-time properties for these dependencies in our framework or put the build-time properties in our application.properties file. That’s the reason for having the `quarkus.hibernate-orm.dialect.mariadb.bytes-per-character` configuration property defined for all the test classes.
* CDI Request Context
    * Due to the https://github.com/quarkusio/quarkus/issues/50154, test that should verify credentials propagation using CDI Request Context is storing state in static variables. This would not work in a concurrent environment.
