#!/usr/bin/env bash
github_url="https://${github_token}@${GH_REF}"

function fix_git {
    echo "Fixing git setup for $TRAVIS_BRANCH"
    git checkout ${TRAVIS_BRANCH}
    git branch -u origin/${TRAVIS_BRANCH}
    git pull origin ${TRAVIS_BRANCH}
    git config branch.${TRAVIS_BRANCH}.remote origin
    git config branch.${TRAVIS_BRANCH}.merge refs/heads/${TRAVIS_BRANCH}
}

function create_bintray_file {
    echo "Creating credentials file"
    if [ -e "$HOME/.bintray/.credentials" ];
    then
        echo "Bintray credentials file already exists"
    else
        mkdir -p "$HOME/.bintray/"
        touch "$HOME/.bintray/.credentials"
        echo "realm = Bintray API Realm" >> "$HOME/.bintray/.credentials"
        echo "host = api.bintray.com" >> "$HOME/.bintray/.credentials"
        echo "user = ${bintray_user}" >> "$HOME/.bintray/.credentials"
        echo "password = ${bintray_password}" >> "$HOME/.bintray/.credentials"
    fi

    if [ -e "$HOME/.bintray/.credentials" ];
    then
        echo "Bintray credentials file successfully created"
    else
        echo "Bintray credentials still not found"
    fi
}

function setup_git_user {
    echo "Setting git user email to ci@outworkers.com"
    git config user.email "ci@outworkers.com"

    echo "Setting git user name to Travis CI"
    git config user.name "Travis CI"
}

function run_publish {
  setup_git_user
  create_bintray_file

  if [ "$PUBLISH_ARTIFACT" == "true" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$TRAVIS_BRANCH" == "develop" ];
    then
        echo "Triggering publish script for Scala $TARGET_SCALA_VERSION";
        sbt "release with-defaults"
    else
        echo "Scala version is not $TARGET_SCALA_VERSION";
        echo "This is either a pull request or the branch is not develop, deployment not necessary"
        exit 0
    fi
}

function run_tests {

  if [ "${PUBLISH_ARTIFACT}" == "true" ];
  then
      echo "Running tests with coverage and report submission"
      sbt "plz $TRAVIS_SCALA_VERSION test"
  else
      echo "Running tests without attempting to submit coverage reports"
      sbt "plz $TRAVIS_SCALA_VERSION test"
  fi

  local exitCode=$?

  if [ ${exitCode} == 0 ]
    then
      echo "Tests successful, running publish script."
      run_publish
      exit $?
    else
      echo "Non zero exit code $exitCode"
      exit ${exitCode}
  fi
}

if [ "$TRAVIS_PULL_REQUEST" == "false" ]
then
    fix_git
else
    echo "Git non shallow cloning not required"
fi

run_tests
