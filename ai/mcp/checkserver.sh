#!/bin/bash

if [ -z $1 ]; then
	echo "no address!"
fi

addr=$1

id=$(curl -v -H "Accept: application/json,text/event-stream" --data '{
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
' ${addr}/mcp | grep -i session-id --color=never | tr -d '\r' )
curl -H "$id" -H "Accept: application/json,text/event-stream" --data '{
  "jsonrpc" : "2.0",
  "id" : 1,
  "method" : "notifications/initialized"
}' ${addr}/mcp
