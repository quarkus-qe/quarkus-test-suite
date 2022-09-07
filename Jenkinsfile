node {
 try{
       ansiColor('xterm'){
         stage 'Checkout'
         checkout scm

         stage 'Destination job calculation'

          def BUILD_NUMBER = "$BUILD_NUMBER"
          sh '''#!/bin/bash +x
                echo "init script ..."
                echo "Build number in sh script: $BUILD_NUMBER"
                echo "PR number: $BRANCH_NAME"
                echo "sha1=$sha1"
                echo "ghprbActualCommit=$ghprbActualCommit"
                echo "ghprbTargetBranch=$ghprbTargetBranch"
                echo "ghprbSourceBranch=$ghprbSourceBranch"
                echo "ghprbPullAuthorEmail=$ghprbPullAuthorEmail"
                echo "ghprbPullDescription=$ghprbPullDescription"
                echo "ghprbPullTitle=$ghprbPullTitle"
                echo "ghprbPullLink=$ghprbPullLink"
                echo "pr ID:"
                echo "ghprbPullId=$ghprbPullId"

                FILES_TRIGGER_ALL="pom.xml"
                MODULES_TRIGGER_ALL=""
                EXCLUSIONS="README.md|.github|.mvn|.gitignore|AUTHORS|COPYRIGHT|Jenkinsfile|LICENSE|mvnw|mvnw.cmd"
                MODULES=$(find -name pom.xml | sed -e 's|pom.xml| |' | sed -e 's|./| |')
                URL="https://api.github.com/repos/quarkus-qe/quarkus-test-suite/pulls/${ghprbPullId}/files"
                FILES=$(curl -u jenkins-ci:${GITHUB_JENKINS_PASSWORD} -s -X GET -G $URL | jq -r '.[] | .filename' | grep -vE $EXCLUSIONS )
                CHANGED=""
                MODULES_ARG=""
                MODULES_CHANGED_AMOUNT=$((MODULES_CHANGED_AMOUNT+0))

                SKIP_MODULE_DETECTION="false"
                for file in $FILES
                do
                    echo "file $file"
                    if [[ $FILES_TRIGGER_ALL =~ ("$file") ]] ; then
                        # We need to check the internal changes to know if trigger all modules or not, the changes that will NOT trigger all modules are:
                        # - New or deleted modules: grep -vE "<module>"
                        # - Empty new lines: grep -v '+[[:space:]]*$'
                        #
                        if [[ "$file" == "pom.xml" ]] ; then
                            # To run the git diff command, we need to retrieve the target branch from origin
                            git fetch origin
                            if [[ $(git --no-pager diff origin/${ghprbTargetBranch} -- pom.xml | grep "+  " | grep -vE "<module>" | grep -v '+[[:space:]]*$') =~ ("+  ") ]] ; then
                                SKIP_MODULE_DETECTION="true"
                            fi
                        else
                            SKIP_MODULE_DETECTION="true"
                        fi
                    fi
                done

                if [[ $SKIP_MODULE_DETECTION == "false" ]] ; then
                    for module in $MODULES
                    do
                        if [[ $FILES =~ ("$module") ]] ; then
                            CHANGED=$(echo $CHANGED" "$module)
                        fi
                    done

                    MODULES_ARG="${CHANGED// /,}"
                    for module in $CHANGED
                    do
                        if [[ $MODULES_TRIGGER_ALL == *"$module"* ]] ; then
                            MODULES_ARG=""
                            break
                        fi
                    done
                fi

                # We want to run the TS only if the affected modules contain OpenShift tests.
                EXECUTE_OPENSHIFT_JOB="false"
                OPENSHIFT_IT_NAME_PATTERN="OpenShift*IT.java"

                # Which modules to check for OpenShift tests: all or only the changed ones.
                MODULES_TO_CHECK_FOR_OPENSHIFT=$MODULES
                if [[ ! -z $MODULES_ARG ]] ; then
                    MODULES_TO_CHECK_FOR_OPENSHIFT=$CHANGED
                fi

                # Does any of the checked modules contain OpenShift tests?
                for module in $MODULES_TO_CHECK_FOR_OPENSHIFT
                do
                    if [[ ! -z $(find "$module" -name "$OPENSHIFT_IT_NAME_PATTERN") ]] ; then
                        EXECUTE_OPENSHIFT_JOB="true"
                        let "MODULES_CHANGED_AMOUNT++"
                    fi
                done

                if [[ $EXECUTE_OPENSHIFT_JOB == "false" ]] ; then
                    # Return non-zero exit code => script condition evaluates to FALSE and the conditional step will be not executed.
                    exit 1
                fi

                if [[ $SKIP_MODULE_DETECTION == "true" ]]; then
                  echo "This change should trigger a matrix job"
                else
                  echo "MODULES with OpenShift test: $MODULES_CHANGED_AMOUNT"
                fi
              '''
       }
   } catch (e) {
 	  currentBuild.result = "FAILED"
 	  throw e
 }
}

