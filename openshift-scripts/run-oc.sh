#!/bin/bash

PRODUCT_RUN=""
RUN_NATIVE=""

while getopts "pns:" opt; do
    case $opt in
        p)  PRODUCT_RUN=1;;
        n)  RUN_NATIVE=1;;
        s)  SCENARIO="$OPTARG";;
        *)
            echo "Usage: ./run-oc.sh [-p] [-n] [-s SCENARIO]"
            echo "-p: product run"
            echo "-n: run native tests"
            exit 1
    esac
done

case $SCENARIO in
    root-modules)                PROFILE='root-modules';;
    http-modules)                PROFILE='http-modules';;
    security-modules)            PROFILE='security-modules';;
    messaging-modules)           PROFILE='messaging-modules';;
    monitoring-modules)          PROFILE='monitoring-modules';;
    databases-modules)           PROFILE='sql-db-modules,nosql-db-modules';;
    spring-modules)              PROFILE='spring-modules';;
    service-binding-modules)     PROFILE='service-binding-modules';;
    misc)                        PROFILE='websockets-modules,test-tooling-modules,cache-modules';;
    *)
        echo "UNKNOWN scenario: $SCENARIO"
        exit 1
esac

docker system prune -a --force --volumes
PATH=${WORKSPACE}:${PATH}

OCP_CLIENT_URL='https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/openshift-client-linux-amd64-rhel8.tar.gz'
wget -O oc.tar -q --no-check-certificate --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 10 ${OCP_CLIENT_URL}
tar x -f oc.tar

if [[ $RUN_NATIVE ]]; then
  MVN_NATIVE_ARGS=" -Dnative"
  MVN_NATIVE_ARGS+=" -Dquarkus.native.container-runtime=docker "
  MVN_NATIVE_ARGS+=" -Dquarkus.native.builder-image=${QUARKUS_NATIVE_BUILDER_IMAGE} "
fi

if [[ $PRODUCT_RUN ]]; then
  wget -q -O quarkus-maven-repo.zip ${MAVEN_REPO_ZIP_URL}
  unzip -q quarkus-maven-repo.zip
  LOCAL_REPO=${WORKSPACE}'/'${MAVEN_REPO_ROOT_DIR_NAME}'/maven-repository/'
  # OCP env info logging - make sure to run BEFORE "oc login"
#  bash ./ocp-env-info.sh ${OCP_URL} ./ocp-env-info.html

  MVN_ADDITIONAL_ARGS+=' -Dmaven.repo.local='${LOCAL_REPO}
  MVN_ADDITIONAL_ARGS+=' -Dts.global.build-number='${BUILD_NUMBER}
  MVN_ADDITIONAL_ARGS+=' -Dts.global.version-number='${QUARKUS_PLATFORM_BOM_VERSION}
  MVN_ADDITIONAL_ARGS+=' -Dts.global.service-name='${JOB_NAME}

  MVN_QUARKUS_VERSION=' -Dquarkus.platform.version='${QUARKUS_PLATFORM_BOM_VERSION}
  MVN_QUARKUS_VERSION+=' -Dquarkus-plugin.version='${QUARKUS_PLATFORM_BOM_VERSION}
  MVN_QUARKUS_VERSION+=' -Dquarkus.platform.group-id=com.redhat.quarkus.platform'
  MVN_QUARKUS_VERSION+=' -Dquarkus.platform.artifact-id=quarkus-bom'
else
  if [[ -n ${QUARKUS_VERSION} ]]; then
      MVN_QUARKUS_VERSION=' -Dquarkus.platform.version='${QUARKUS_VERSION}
  fi
fi

oc login ${OCP_URL} --username=${OCP_USERNAME} --password=${OCP_PASSWORD} --insecure-skip-tls-verify

mvn -B --no-transfer-progress -V -fae clean verify -Dlog.nocolor=true\
        -P ${PROFILE} \
        ${MVN_ADDITIONAL_ARGS} \
        ${MVN_NATIVE_ARGS} \
        ${MVN_QUARKUS_VERSION} \
        -Dts.openshift.ephemeral.namespaces.enabled=true \
        -Dopenshift -Dinclude.operator-scenarios \
        -Dinclude.serverless \
        -Dts.container.registry-url=quay.io/quarkusqeteam \
        -Dts.global.s2i.maven.remote.repository=${MVN_REMOTE_REPO_URL}