#!/usr/bin/env bash
github_url="https://${github_token}@${GH_REF}"
scala_version="2.12.3"

function fix_git {
    echo "Fixing git setup for $TRAVIS_BRANCH"
    git checkout ${TRAVIS_BRANCH}
    git branch -u origin/${TRAVIS_BRANCH}
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

function publish_to_bintray {
  setup_git_user
  create_bintray_file

  COMMIT_MSG=$(git log -1 --pretty=%B 2>&1)
  COMMIT_SKIP_VERSION="[version skip]"
  COMMIT_SKIP_PUBLISH="[publish skip]"

  echo "Last commit message $COMMIT_MSG"
  echo "Commit skip message $COMMIT_SKIP_VERSION"

  if [[ $COMMIT_MSG == *"$COMMIT_SKIP_VERSION"* ]]
  then
      echo "Skipping version bump and simply tagging"
  else
      echo "Bumping version bump and simply tagging"
      #sbt version-bump-patch git-tag
  fi

  if [[ $COMMIT_MSG == *"$COMMIT_SKIP_PUBLISH"* ]]
  then
      echo "Skipping publishing"
  else
      echo "Publishing new version to Bintray"
      sbt "release with-defaults"
  fi
}

function run_publish {
  if [ "$TRAVIS_SCALA_VERSION" == ${scala_version} ] &&
    [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$TRAVIS_BRANCH" == "develop" ];
    then
        echo "Triggering publish script for Scala $scala_version";
        publish_to_bintray
        exit $?
    else
        echo "Scala version is not $scala_version";
        echo "This is either a pull request or the branch is not develop, deployment not necessary"
        exit 0
    fi
}

function run_tests {

  if [ "${TRAVIS_SCALA_VERSION}" == ${scala_version} ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
  then
      echo "Running tests with coverage and report submission"
      sbt "+++$TRAVIS_SCALA_VERSION test"
  else
      echo "Running tests without attempting to submit coverage reports"
      sbt "+++$TRAVIS_SCALA_VERSION test"
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

fix_git
run_tests
