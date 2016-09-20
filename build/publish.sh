#!/usr/bin/env bash
github_url="https://${github_token}@${GH_REF}"

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
    if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "develop" ];
        then
            setup_git_user
            create_bintray_file
            sbt version-bump-patch git-tag

            echo "Pushing tag to GitHub."
            git push ${github_url} --tags

            echo "Pushing"
            git add .
            git commit -m "TravisCI: Bumping version [ci skip]"

            git checkout -b version_branch
            git checkout -B develop version_branch

            git push ${github_url} develop

            echo "Publishing new version to Bintray"
            sbt +bintray:publish
    else
        echo "This is either a pull request or the branch is not develop, deployment not necessary"
    fi
}

if [ "$TRAVIS_SCALA_VERSION" == "2.11.8" ];
    then
        echo "Triggering publish script for Scala 2.11.8";
        publish_to_bintray
else
    echo "Scala version is not 2.11.8";
    exit 0
fi
