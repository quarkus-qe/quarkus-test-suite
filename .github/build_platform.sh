#!/bin/bash
set -xeuo pipefail

#QUARKUS_BUILD=999-SNAPSHOT
#LANGCHAIN4J_BUILD=999-SNAPSHOT
#MCP_BUILD=999-SNAPSHOT
#PLATFORM_BUILD=999-core-main-SNAPSHOT

# This output also allows us to fail early if any variable is unset
echo "Building platform ${PLATFORM_BUILD} using Quarkus Core ${QUARKUS_BUILD}, Langchain4j ${LANGCHAIN4J_BUILD} and MCP server ${MCP_BUILD}"

./mvnw versions:set -DnewVersion=${PLATFORM_BUILD}
./mvnw versions:set-property -Dproperty=quarkus.version -DnewVersion=${QUARKUS_BUILD} -DgenerateBackupPoms=false
./mvnw versions:set-property -Dproperty=quarkus-langchain4j.version -DnewVersion=${LANGCHAIN4J_BUILD} -DgenerateBackupPoms=false
./mvnw versions:set-property -Dproperty=quarkus-mcp-server.version -DnewVersion=${MCP_BUILD} -DgenerateBackupPoms=false

if [ ! $(which xsltproc) ]; then
  echo "xsltproc is not installed!"
  exit 1
fi

# mark everything as disabled
xsltproc -o pom.xml minimise_platform.xsl pom.xml
./mvnw -Dsync
#same script but with standard bash
#grep -A1 '<member>' pom.xml | grep name | grep -iv -e 'langchain4j' -e 'mcpserver' | xargs -I '{}' sed -i '\|{}|a <enabled>false<\/enabled>' pom.xml
./mvnw clean -V install -DskipTests -DskipITs -Prelease -Dgpg.skip=true
