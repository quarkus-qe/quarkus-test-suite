# MCP Server and Client

Verifies, that Quarkus implements MCP specification properly on server and client side.

Some notes:
- There are three transport protocols: stdio, http and websocket 
- HTTP is a part of standard, but there are some issues with server-sent requests (eg. https://github.com/quarkiverse/quarkus-langchain4j/issues/2159). At the same time, this one is the easiest for standalone reproducers, using curl and jq.
- Stdio is a part of standard, but only works locally and also the slowest one (Quarkus QE framework can not stop the server properly, so it has to be killed on timeout)
- WebSocket transport is not a part of standard, and standalone reproducers are pain to make, but it has the most robust support on server side.
- Quarkus client only supports subset of server features.

Thus, the architecture of this module is like this:
- BasicMCPIT tests features, which are supported on both Quarkus MCP client and server sides.
- Stdio test class runs only tests from BasicMCPIT
- HTTP classes have some additional tests for third-party clients (we use the official one: https://github.com/modelcontextprotocol/java-sdk)
- WebSocket classes run both basic tests and tests for server-only features. They use custom client (see Session class for details).

Useful tips for making a standalone reproducer:
- add property `quarkus.profile=debug`, you will see jsons, which are send between client and server and session info.
  - `-Dts.global.delete.folder.on.exit=false` option saves applications to `target/$TESTNAME/client/mvn-build` and to `target/$TESTNAME/server/mvn-build`  
- For some server features the most convenient way is to run websocket tests, copy jsons and then use curl and http server to reproduce.
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
