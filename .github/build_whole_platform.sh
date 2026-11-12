#!/bin/bash
set -xeuo pipefail

if [ ! $(which xsltproc) ]; then
  echo "xsltproc is not installed!"
  exit 1
fi

if [ ! $(which yq) ]; then
  echo "yq is not installed!"
  exit 1
fi

usage() {
  echo "Usage: $0 --settings <maven-settings.xml> --repo <local-maven-repo-dir> --config <platform-config-yaml> --minimiser <xsl-file>"
  exit 1
}

SETTINGS=""
REPO=""
CONFIG=""
MINIMISER=""
while [ $# -gt 0 ]; do
  case "$1" in
    --settings) SETTINGS="${2:-}"; shift 2 ;;
    --repo) REPO="${2:-}"; shift 2 ;;
    --config) CONFIG="${2:-}"; shift 2 ;;
    --minimiser) MINIMISER="${2:-}"; shift 2 ;;
    -h|--help) usage ;;
    *) echo "Unknown argument: $1"; usage ;;
  esac
done
[ -n "${SETTINGS}" ] && [ -n "${REPO}" ] && [ -n "${CONFIG}" ] && [ -n "${MINIMISER}" ] || usage

CORE_BRANCH=$(yq eval '.core.branch' ${CONFIG})
QUARKUS_BUILD==$(yq eval '.core.version' ${CONFIG})

LANGCHAIN4J_BRANCH=$(yq eval '.langchain4j.branch' ${CONFIG})
LANGCHAIN4J_BUILD=$(yq eval '.langchain4j.version' ${CONFIG})

MCP_BRANCH=$(yq eval '.mcp.branch' ${CONFIG})
MCP_BUILD=$(yq eval '.mcp.version' ${CONFIG})

HTTP_PROBLEM_BRANCH=$(yq eval '.httpp.branch' ${CONFIG})
HTTP_PROBLEM_BUILD=$(yq eval '.httpp.version' ${CONFIG})

PLATFORM_BRANCH=$(yq eval '.platform.branch' ${CONFIG})
PLATFORM_BUILD=$(yq eval '.platform.version' ${CONFIG})
# This output also allows us to fail early if any variable is unset
echo "Building platform ${PLATFORM_BUILD} using Quarkus Core ${QUARKUS_BUILD}, Langchain4j ${LANGCHAIN4J_BUILD}, MCP server ${MCP_BUILD} and HTTP Problem ${HTTP_PROBLEM_BUILD}"

BASE_DIR=${PWD}
# ====================
echo "Building core ${CORE_BRANCH}"

git clone -b ${CORE_BRANCH} https://github.com/quarkusio/quarkus.git && cd quarkus
./mvnw -B -ntp -s "${SETTINGS}" clean install -Dquickly -Dno-test-modules -Prelocations -Dinsecure.repositories=WARN -Dmaven.repo.local="${REPO}" -DaltDeploymentRepository=local::file:"${REPO}" -DdeployAtEnd
cd ${BASE_DIR}
# ====================
echo "Build Quarkus Langchain4j ${LANGCHAIN4J_BUILD}"

git clone -b ${LANGCHAIN4J_BRANCH} https://github.com/quarkiverse/quarkus-langchain4j.git && cd quarkus-langchain4j
mvn versions:set -DnewVersion=${LANGCHAIN4J_BUILD} -DgenerateBackupPoms=false -B -ntp -s "${SETTINGS}"
# integration tests depend on platform by default (see https://github.com/quarkiverse/quarkus-langchain4j/issues/2633)
mvn versions:set-property -Dproperty=quarkus.platform.version -DnewVersion=${QUARKUS_BUILD} -DgenerateBackupPoms=false -B -ntp -s "${SETTINGS}"
mvn versions:set-property -Dproperty=quarkus.platform.group-id -DnewVersion=io.quarkus -DgenerateBackupPoms=false -B -ntp -s "${SETTINGS}"
mvn -B -ntp -s "${SETTINGS}" clean install -DskipTests -Dquarkus.version=${QUARKUS_BUILD} -Dmaven.repo.local="${REPO}" -DaltDeploymentRepository=local::file:"${REPO}" -DdeployAtEnd
cd ${BASE_DIR}
# ====================
echo "Build Quarkus MCP Server ${MCP_BUILD}"
git clone -b ${MCP_BRANCH} https://github.com/quarkiverse/quarkus-mcp-server.git && cd quarkus-mcp-server
mvn versions:set -DnewVersion=${MCP_BUILD} -DgenerateBackupPoms=false  -B -ntp -s "${SETTINGS}"
mvn -B -ntp -s "${SETTINGS}" clean install -DskipTests -Dquarkus.version=${QUARKUS_BUILD} -Dmaven.repo.local="${REPO}" -DaltDeploymentRepository=local::file:"${REPO}" -DdeployAtEnd
cd ${BASE_DIR}
# ====================
echo "Build Quarkus HTTP Problem ${HTTP_PROBLEM_BUILD}"
git clone -b ${HTTP_PROBLEM_BRANCH} https://github.com/quarkiverse/quarkus-http-problem.git && cd quarkus-http-problem
mvn versions:set -DnewVersion=${HTTP_PROBLEM_BUILD} -DgenerateBackupPoms=false  -B -ntp -s "${SETTINGS}"
mvn -B -ntp -s "${SETTINGS}" clean install -DskipTests -Dquarkus.version=${QUARKUS_BUILD} -Dmaven.repo.local="${REPO}" -DaltDeploymentRepository=local::file:"${REPO}" -DdeployAtEnd
cd ${BASE_DIR}
# ====================
git clone -b https://github.com/quarkusio/quarkus-platform.git && cd quarkus-platform
./mvnw versions:set -ntp -DnewVersion=${PLATFORM_BUILD} -s "${SETTINGS}"
./mvnw versions:set-property -ntp -Dproperty=quarkus.version -DnewVersion=${QUARKUS_BUILD} -DgenerateBackupPoms=false -s "${SETTINGS}"
./mvnw versions:set-property -ntp -Dproperty=quarkus-langchain4j.version -DnewVersion=${LANGCHAIN4J_BUILD} -DgenerateBackupPoms=false -s "${SETTINGS}"
./mvnw versions:set-property -ntp -Dproperty=quarkus-mcp-server.version -DnewVersion=${MCP_BUILD} -DgenerateBackupPoms=false -s "${SETTINGS}"
./mvnw versions:set-property -ntp -Dproperty=quarkus-http-problem.version -DnewVersion=${HTTP_PROBLEM_BUILD} -DgenerateBackupPoms=false -s "${SETTINGS}"

# mark everything as disabled
xsltproc -o pom.xml minimise_platform.xsl pom.xml
#same script but with standard bash
#grep -A1 '<member>' pom.xml | grep name | grep -iv -e 'langchain4j' -e 'mcpserver' | xargs -I '{}' sed -i '\|{}|a <enabled>false<\/enabled>' pom.xml

./mvnw -ntp -Dsync -s "${SETTINGS}" -Dmaven.repo.local="${REPO}"
./mvnw -ntp clean -V install -DskipTests -DskipITs -Prelease -Dgpg.skip=true -s "${SETTINGS}" -Dmaven.repo.local="${REPO}"
