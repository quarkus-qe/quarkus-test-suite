#!/bin/bash

if [[ $# != 2 ]]; then
  echo "ERROR: Invalid number of arguments. Usage: $0 <OCP_URL> <OUTPUT_FILE>"
  exit 1
fi

OCP_URL=$1
OUTPUT_FILE=$2

OCP_USERNAME=""
OCP_PASSWORD=""

oc login "${OCP_URL}" --username=${OCP_USERNAME} --password=${OCP_PASSWORD} --insecure-skip-tls-verify

# Detect OCP platform version
PLATFORM_VERSION="$(oc get clusterversion -o jsonpath='{.items[].status.desired.version}')"
if [[ ${PLATFORM_VERSION} == "" ]]; then
  echo "ERROR: OCP platform version could not be detected."
fi

# Parse "<operator-name>.v<operator-version>" to "<operator-name>=<operator-version>"
OPERATOR_VERSION_SED_SCRIPT="s|\([^.]\+\)\.v\(.*\)|\1:\2|"

# Detect OCP operators versions
OPERATORS_VERSIONS="$(oc get csv --no-headers --output=custom-columns=name:.metadata.name | sed "${OPERATOR_VERSION_SED_SCRIPT}" | sort)"
if [[ ${OPERATORS_VERSIONS} == "" ]]; then
  echo "ERROR: OCP operators versions could not be detected."
fi

{
  printf "<li>%s\n  <ul>\n" "$PLATFORM_VERSION"
  for OPERATOR in $OPERATORS_VERSIONS
  do
    printf "    <li>%s</li>\n" "$OPERATOR"
  done
  printf "  </ul>\n</li>\n"
} > "$OUTPUT_FILE"