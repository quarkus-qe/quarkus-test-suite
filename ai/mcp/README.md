# MCP Server and Client

Tests MCP server features using Streamable HTTP, stdio and WebSocket. HTTP and
stdio are standard MCP transports; WebSocket is a Quarkus extension.

The existing scenarios use `2025-11-25`. The `Stateless*IT` scenarios use
`2026-07-28`. Tests check actual protocol use through server observations or client initialization
results; stdio forwards compact observations through stderr.

- LangChain4j clients test tools, resources, prompts and dynamic tool changes.
- McpAssured tests the server directly, including pagination, icons, notifications,
  sampling, elicitation, completion, progress and logging.
- The Java SDK provides separate HTTP tests for `2025-11-25`.
- HTTP scenarios also test OIDC authentication and named providers.

`StatefulMCPIT` contains the older roots callback test. `StatelessMCPIT`
and `StatelessAdvancedMCPIT` share newer direct-server tests across the transports.
The HTTP scenario also tests headers and protocol selection.

Stdio scenarios launch a JVM server subprocess, including when the client wrapper
runs as a native executable.

Run the local JVM scenarios from the repository root:

```sh
MAVEN_ARGS=-nsu mvn -nsu -s .github/mvn-settings.xml -pl ai/mcp \
  -Dit.test=LocalStreamingHTTPIT,WebSocketIT,StdioIT,OIDCIT,OIDCClientIT,NamedOIDCClientIT \
  -Dreruns=0 -Dno-format verify
MAVEN_ARGS=-nsu mvn -nsu -s .github/mvn-settings.xml -pl ai/mcp \
  -Dit.test=StatelessStreamingHTTPIT,StatelessWebSocketIT,StatelessStdioIT,StatelessOIDCIT,StatelessOIDCClientIT,StatelessNamedOIDCClientIT \
  -Dreruns=0 -Dno-format verify
```

Useful tips for making a standalone reproducer:
- (if you're using OpenShift) change deployment strategy to registry (which we usually do not use due to issues with cross-compilation for ARM)
- add property `quarkus.profile=debug`, you will see jsons, which are send between client and server and session info.
  - `-Dts.global.delete.folder.on.exit=false` option saves applications to `target/$TESTNAME/client/mvn-build` and to `target/$TESTNAME/server/mvn-build`  
- For some server features the most convenient way is to run websocket tests, copy jsons and then use curl and http server to reproduce.
These examples use the older `2025-11-25` initialization flow.

Some curl examples
```
# As a first step, always initialize the client (some of `capabilities` may be omitted, dependending on use case)
id=$(curl -i -H "Accept: application/json,text/event-stream" --data '{
  "jsonrpc" : "2.0",
  "id" : 0,
  "method" : "initialize",
  "params" : {
    "protocolVersion" : "2025-11-25",
    "clientInfo" : {
      "name" : "custom",
      "version" : "1.0"
    },
    "capabilities" : {
      "sampling" : {
        "tools" : { }
      },
      "elicitation" : {
        "form" : { },
        "url" : { }
      }
    }
  }
}
' localhost:8080/mcp | grep -i session-id --color=never | tr -d '\r' )
# You may use curl without pipelining and wrappers, if you do not need session id (see below)

# Some features only work if you notify server, that the client is initialized, so we are sending the request below as well 
# Here I include session id in headers. It is required for cases, where server initializes data exchange (eg elicitation and sampling)
# in such a case, include the header in each request
# do not forget to increment the `id` field in every new request
curl -H "$id" -H "Accept: application/json,text/event-stream" --data '{
  "jsonrpc" : "2.0",
  "id" : 1,
  "method" : "notifications/initialized"
}' localhost:8080/mcp

# some tools return only first N results and add `nextCursor` field. If you want to retireve more, then this cursor should be added as a parameter to the next call
curl -H "Accept: application/json,text/event-stream" --data '{
  "jsonrpc" : "2.0",
  "id" : 1,
  "method" : "tools/list"
 }' localhost:8080/mcp | jq ".result.tools[].name, .result.nextCursor"`

 curl -H "Accept: application/json,text/event-stream" --data '{
   "jsonrpc" : "2.0",
   "id" : 2,
   "method" : "tools/list",
   "params" : {
     "cursor" : "MjAyNi0wMy0xMFQxNjoyMTo0OS41MTcwMDY5MzdaJCQkc2FtcGxlZA=="
   }

  }' localhost:8080/mcp `| jq ".result.tools[].name, .result.nextCursor"`
```
