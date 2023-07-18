# Build-time analytics

Verifies the Quarkus build-time analytics feature: https://quarkus.io/guides/build-analytics.

The module uses Quarkus CLI to create/build applications and verify various scenarios of the analytics functionality.
For scope of testing, see related test plan https://github.com/quarkus-qe/quarkus-test-plans/blob/main/QUARKUS-2812.md
and feature request https://issues.redhat.com/browse/QUARKUS-2812.

Most of the tests invoke dev or prod build on the generated application and verify the presence/absence of a build
analytics payload file and its contents. All the tests are using a custom `quarkus.analytics.uri.base` (see the upstream
docs) to prevent Quarkus from actually sending data to the analytics service.

Due to the usage of Quarkus CLI, this module is similar to the `quarkus-cli` module and also expects the Quarkus CLI
command to be available in the environment and to be called `quarkus` by default or configured by the
`-Dts.quarkus.cli.cmd=/path/to/quarkus-dev-cli` property.

Unlike the `quarkus-cli`, this module is not affected by the `include.quarkus-cli-tests` / `exclude.quarkus.cli.tests`
properties, because the test classes names do not match the `**/QuarkusCli*IT.java` pattern.

The test scenarios in this module are labeled with `@Tag("quarkus-cli")`, so that they can be grouped with other
CLI-based tests.

To simulate different user scenarios, the tests modify environment setup by interacting with the user-specific
build-time analytics configuration located in `<user.home>/.redhat/`.
