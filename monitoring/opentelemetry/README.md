# Quarkus OpenTelemetry extension

## Guide
[Quarkus OpenTelemetry guide](https://quarkus.io/guides/opentelemetry)

## Scope of the test
1. Testing OpenTelemetry with Jaeger components
 - Extension `quarkus-opentelemetry` - responsible for traces generation in OpenTelemetry format and export into OpenTelemetry components (opentelemetry-agent, opentelemetry-collector)
 
Scenarios that test proper traces export to Jaeger components and context propagation. 
Implementation: two REST services, one Jaeger all-in-one pod (creating jaeger-rest & jaeger-query services).  
Ping Pong application with 3 pods (ping service, pong service, jaeger-all-in-one). Traces are send directly into jaeger-collector (no local jaeger-agent process in Quarkus pods)