# Openshift CI

This folder contains files, required for OpenShift CI.
It allows us to verify, that Quarkus works on new version of Openshift.

## How it works

The tests are run regularly (every Monday and the time of writing). Test execution is based on configs: [[1]](https://github.com/openshift/release),[[2]](https://github.com/openshift/release/pull/40279/files).

During the run CI takes the Dockerfile from this folder, builds an image from it and runs tests from modules from `$PROJECTS` variable.

Results are posted into [quarkus-qe](https://redhat-internal.slack.com/archives/C05CMLUAWTT) channel in Slack and new issue in [QQE Jira](https://issues.redhat.com/projects/QQE/summary) is created automatically for every failure. Job history can be accessed from the [Prow Dashboard](https://prow.ci.openshift.org/job-history/gs/origin-ci-test/logs/periodic-ci-quarkus-qe-quarkus-test-suite-main-quarkus-ocp4.14-lp-interop-quarkus-interop-aws) or via [dashboard](https://testgrid.k8s.io/redhat-openshift-lp-interop-release-4.14-informing#periodic-ci-quarkus-qe-quarkus-test-suite-main-quarkus-ocp4.14-lp-interop-quarkus-interop-aws&width=90).

## Requirements
### Released version of Quarkus
Due to the nature of tests, only the released bits are used. These are pulled directly from the maven.repository.redhat.com, not from an internal source, to be as close to real customer use-case as possible. This means that new versions of Quarkus can only be tested once the release is done and the artifacts are in the maven repository.

### OpenShift requirements
There are no specific requirement on OpenShift installation, but pull secret for dockerhub is highly recommended to avoid pull rate limiting.

### Test machine requirements
All the required libraries are installed as part of the scenario:
- Git
- Java 11 OpenJDK
- Maven
- Docker/Podman/Buildah

## Processes
### Contacting PIT team
- Email: [pit-qe@redhat.com](mailto:pit-qe@redhat.com)
- Jira: [INTEROP project](https://projects.engineering.redhat.com/browse/INTEROP)
- Slack: [#forum-qe-layered-product](https://redhat-internal.slack.com/archives/C04QDE5TK1C)

### New version of Quarkus
After a new version of Quarkus is released, you should do the following:
- Update this section of Dockerfile (description is in the comments):
```asciidoc
ENV QUARKUS_BRANCH=2.13 # branch in this repo which is used to run tests
ENV QUARKUS_VERSION=2.13.7.Final-redhat-00003 # version of Quarkus BOM associated with the release
ENV QUARKUS_PLATFORM_GROUP_ID=com.redhat.quarkus.platform # group of Quarkus BOM. Unlikely to change
ENV QUARKUS_PLATFORM_ARTIFACT_ID=quarkus-bom # name of Quarkus BOM. Unlikely to change
```
- Create PR to `main` branch and pay attention to `ci/prow/quarkus-ocp4.14-lp-interop-images` check.
- Merge the PR

**NB:** For now, Interop team supports only one job for every product, so we only test the latest RHBQ release.

### Adding new tests
Currently, the list of modules to be run is defined in the run script in `PROJECTS` variable. If you want additional tests to be run, update the list of modules (don't forget to add dependent modules if applicable).
You should only add stable tests without random failures to the list! Once done, run the tests locally against our OCP instance, when follow the same steps above.

## How to run locally
- Change `oc_login.sh` to login to your existing OCP cluster.
- Create a new public Docker repository (eg `quay.io/$USER/test-container`)
- Build and save an image for testing (you can use Docker, Podman or Buildah):
```
podman build --tag=quay.io/$USER/test-container -f Dockerfile 
podman push quay.io/$USER/test-container
```
- Run the tests:
```
oc create deployment interop-container --image=quay.io/$USER/test-container
```
- Clean after yourself:
```
oc delete deployment interop-container
```
