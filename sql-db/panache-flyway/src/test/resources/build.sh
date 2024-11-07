#!/bin/bash
# based on these guides: https://blog.while-true-do.io/podman-multi-arch-images/ and https://community.ibm.com/community/user/powerdeveloper/blogs/mayur-waghmode/2022/09/19/building-multi-arch-container-images-with-github-a

set -euxo pipefail

version=${1}
if [ -z "$version" ]; then
        echo "Provide version for https://quay.io/repository/quarkusqeteam/wait container!"
        exit 1
fi
TAG="quay.io/quarkusqeteam/wait:$version"
podman manifest create $TAG
podman build . --platform linux/amd64,linux/arm64,linux/ppc64le,linux/s390x --manifest $TAG  --file Dockerfile
podman manifest push $TAG
