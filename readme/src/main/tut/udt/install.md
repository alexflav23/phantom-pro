Phantom pro [![Build Status](https://travis-ci.com/outworkers/phantom-pro.svg?token=tyRTmBk14WrDycpepg9c&branch=develop)](https://travis-ci.com/outworkers/phantom-pro.svg?token=tyRTmBk14WrDycpepg9c&branch=develop)
================================================================================================================================================

![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Outworkers Phantom Pro")

Available modules
=================

This is a table of the available modules for the various Scala versions. Not all modules are available for all versions just yet, and this is because certain dependencies have yet to be published for Scala 2.12.

| Module name           | Scala 2.10.x        | Scala 2.11.x      | Scala 2.12.0      | Released      |
| ------------          | ------------------- | ------------------| ----------------- | ------------- |
| phantom-autotables    | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | yes           |
| phantom-dse           | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | June 2017     |
| phantom-graph         | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | Sept 2017     |
| phantom-migrations    | <span>yes</span>    | <span>no</span>  | <span>yes</span>   | June 2017     |
| phantom-spark         | <span>yes</span>    | <span>yes</span> | <span>no</span>    | December 2017 |
| phantom-udt           | <span>yes</span>    | <span>yes</span> | <span>yes</span>   | yes           |
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

    val phantomPro = "0.5.0"

    libraryDependencies ++= Seq(
      // Compile time only dependency
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      "com.outworkers" %% "phantom-udt" % Versions.phantomPro
    )

```

### Using Phantom UDT



```scala
import com.outworkers.phantom.udt._

@Udt case class Record(
  id: UUID,
  text: String,
  items: List[String]
)

@Udt case class RecordCollection(
  id: UUID,
  records: Set[Record]
)

```

### Creating the schema for UDTs

At this point in time, phantom-pro does not offer automated initilization of UDT
types as part of the `Database.create` execution chain, but it does however offer
tooling required to auto-generate the UDT schema.

In our own tests, you may want to have a look at `TestDatabase.scala` below.
where we inspect our UDT hierarchy and manually initiaze the the types in the correct order.

This will be automated in a future release. For now, using `ExecutableStatementList` allows
you to execute queries against the database in sequence. The below code snippet
demonstrates a potential UDT hierarchy and how to initialize it.

In this example, we override the default `createAsync` method of a database
and make sure to initialize UDT types before the Cassandra table definitions
in the database. In this example, we rely on the `sequentialFuture` method
of `ExecutableStatementList` to execute schema queries one at a time in the
order they were defined.

```tut:silent

import scala.concurrent.ExecutionContextExecutor
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.builder.query.QueryCollection

class TestDatabase(
  override val connector: CassandraConnection
) extends Database[TestDatabase](connector) {

  def initUds: QueryCollection[Seq] = {
    new ExecutableStatementList[Seq](
      Seq(
        UDTPrimitive[Record].schemaQuery(),
        UDTPrimitive[RecordCollection].schemaQuery()
      )
    )
  }
}

override def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
  initUds.sequentialFuture() flatMap(_ => super.createAsync())
}
```

Now you can freely use UDTs as part of the standard DSL.
