Phantom pro [![Build Status](https://travis-ci.com/outworkers/phantom-pro.svg?token=tyRTmBk14WrDycpepg9c&branch=develop)](https://travis-ci.com/outworkers/phantom-pro.svg?token=tyRTmBk14WrDycpepg9c&branch=develop)
================================================================================================================================================

![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Outworkers Phantom Pro")

Available modules
=================

This is a table of the available modules for the various Scala versions. Not all modules are available for all versions just yet, and this is because certain dependencies have yet to be published for Scala 2.12.

| Module name           | Scala 2.10.x        | Scala 2.11.x      | Scala 2.12.4      | Released      |
| ------------          | ------------------- | ------------------| ----------------- | ------------- |
| phantom-udt           | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | October 2016  |
| phantom-autotables    | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | June 2017     |
| phantom-dse           | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | Dec 2016      |
| phantom-migrations    | <span>yes</span>    | <span>yes</span>  | <span>yes</span>   | June 2017     |
| phantom-graph         | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | Sept 2017     |
| phantom-spark         | <span>yes</span>    | <span>yes</span> | <span>no</span>    | Dec 2017      |
| phantom-solr          | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | August 2017   |
| phantom-native        | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | Oct 2017      |
| phantom-java-dsl      | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | Oct 2017      |


This is the installation guideline for the professional edition of phantom. Thank you for purchasing a subscription.

### Installation instructions

- [ ] Setup a Bintray OSS account.
- [ ] Setup Bintray credentials locally on the development machines.
- [ ] Add the Outworkers enterprise resolver to `build.sbt`
- [ ] Add the dependency on phantom-pro to the build.

#### Setting up a Bintray OSS account.

We use Bintray to distribute our products, the product is a JAR. Include this dependency in the build file, the same way you would include other dependencies.

To sign up to this free and automated service, simply navigate to [https://bintray.com/signup/oss](https://bintray.com/signup/oss).

#### Setting up Bintray credentials locally

First, let's set up the account credentials on a development machine.

This is to identify the license and grant access to the Outworkers enterprise release repository.

A local file must created. By convention this is located at `~/.bintray/.credentials`, and it contains:

```text
realm=Bintray
host=dl.bintray.com
user=$username
password=PASTEHERE
```

The `$username` is the Bintray username. The `$apiKey` is retrievable from within the Bintray UI following the below steps.


##### Locating the Bintray API key

- Sign in to Bintray: [https://bintray.com/login](https://bintray.com/login).

![phantom](https://s3-us-west-2.amazonaws.com/outworkers.images/phantom-pro/step1.png "Step 1")

- Navigate to [https://bintray.com/profile/edit](https://bintray.com/profile/edit).
Click on the profile icon in the top right corner and then select "Edit profile".

![phantom](https://s3-us-west-2.amazonaws.com/outworkers.images/phantom-pro/step2.png "Step 2")

- Next, there will be a new menu on the left hand side, with the last item being the "API Key". Simply navigate to
this page and enter the account password again when prompted. After inputting the password,
the "Show API Key" button will be revealed.

![phantom](https://s3-us-west-2.amazonaws.com/outworkers.images/phantom-pro/step3.png "Step 3")

- Click and the API key will become visible.

![phantom](https://s3-us-west-2.amazonaws.com/outworkers.images/phantom-pro/step4.png "Step 4")

- Copy the key to `~/.bintray/.credentials` in the appropriate location.

![phantom](https://s3-us-west-2.amazonaws.com/outworkers.images/phantom-pro/step5.png "Step 5")

```text
realm=Bintray
host=dl.bintray.com
user=$username
password=API_KEY_GOES_HERE
```

##### Adding credentials to the build.

With SBT, there is one last step before, linking credentials with the build definition in `build.sbt`.

To do this, add a reference to the credentials file from within `build.sbt`. It can go at the top.

```scala
  credentials += Credentials(Path.userHome / ".bintray" / ".credentials")
  // ...
  lazy val project = Project(...)
```

##### Adding the Outworkers resolver to `build.sbt`

This stage ensures SBT and Ivy will look in the Outworkers repository for dependencies.

```scala
  resolvers ++= Seq(
    Resolver.typesafeRepo("releases"),
    ...,
    Resolver.bintrayRepo("outworkers", "enterprise-releases")
  )
```

#### Add the dependency on phantom pro to the build

The last step is adding the dependency to the relevant project.
If using UDTs or autotables, the `macro-paradise` compiler plugin is required.
This is a compile time only dependency, which doesn't affect runtime.

This should be included in the sub-module that requires the dependency. If there are multiple
modules in the build, this should only be added to the relevant sub-modules.

```scala

    val phantomPro = "0.3.0"

    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      "com.outworkers" %% "phantom-udt" % Versions.phantomPro
    )

```
