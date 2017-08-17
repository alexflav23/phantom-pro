import sbt._
import Keys._
import ReleaseTransformations._

lazy val Versions = new {
  val phantom = "2.14.0"
  val util = "0.37.0"
  val logback = "1.2.1"
  val dse = "1.1.0"
  val scalaTest = "3.0.1"
  val scalactic = "3.0.3"
  val shapeless = "2.3.2"
  val scalaMeter = "0.8.3"
  val spark = "1.6.0"
  val dseDriver = "1.1.0"
  val macroCompat = "1.1.1"
  val macroParadise = "2.1.0"
  val scalaGraph = "1.11.4"
  val dockerKit = "0.9.0"
  val scala210 = "2.10.6"
  val scala211 = "2.11.11"
  val scala212 = "2.12.3"
  val monix = "2.3.0"
  val scalaAll = Seq(scala210, scala211, scala212)
}

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := Versions.scala212,
  fork in Test := true,
  testOptions in Test += Tests.Argument("-oF"),
  logBuffered in Test := false,
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, 4)
  ),
  libraryDependencies ++= Seq(
     "ch.qos.logback" % "logback-classic" % Versions.logback % Test,
      "org.scalactic" %% "scalactic" % Versions.scalactic % Test
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.bintrayRepo("outworkers", "oss-releases"),
    Resolver.bintrayRepo("outworkers", "internal-releases")
  ),
  commands in ThisBuild += Command.command("testsWithCoverage") { state =>
    "coverage" ::
    "test" ::
    "coverageReport" ::
    "coverageAggregate" ::
    "coveralls" ::
    state
  },
  scalacOptions in ThisBuild ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-language:experimental.macros",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  javaOptions in Test ++= Seq(
    "-Xmx2G",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.resourceLeakDetection"
  )
) ++ Publishing.effectiveSettings

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val phantomPro = (project in file("."))
  .settings(sharedSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    moduleName := "phantom-pro"
  )
  .aggregate(
    phantomDse,
    phantomDseGraph,
    phantomMigrations,
    phantomUdt,
    phantomAutoTables,
    phantomDocker
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomDse = (project in file("phantom-dse"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Versions.scalaAll,
    moduleName := "phantom-dse",
    libraryDependencies ++= Seq(
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.datastax.cassandra"       %  "dse-driver"                       % Versions.dse,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomMigrations = (project in file("phantom-migrations"))
  .settings(sharedSettings: _* )
  .settings(
    crossScalaVersions := Versions.scalaAll,
    moduleName := "phantom-migrations",
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % Versions.macroCompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers" %% "phantom-dsl" % Versions.phantom,
      "com.outworkers"  %% "util-testing" % Versions.util % Test
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomAutoTables = (project in file("phantom-autotables"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Versions.scalaAll,
    moduleName := "phantom-autotables",
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "com.outworkers" 							%% "phantom-dsl" 										  % Versions.phantom,
      "com.outworkers"              %% "util-testing"                     % Versions.util % Test
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomDseGraph = (project in file("phantom-graph"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-graph",
    crossScalaVersions := Versions.scalaAll,
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       % "dse-driver"                        % Versions.dseDriver,
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomDocker = (project in file("phantom-docker"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-docker",
    publishMavenStyle := false,
    sbtPlugin := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    crossScalaVersions := Seq(Versions.scala210),
    libraryDependencies ++= Seq(
      "com.whisk" %% "docker-testkit-scalatest" % Versions.dockerKit,
      "com.whisk" %% "docker-testkit-impl-spotify" % Versions.dockerKit
    )
  ).enablePlugins(CrossPerProjectPlugin)

lazy val phantomUdt = (project in file("phantom-udt"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-udt",
    crossScalaVersions := Versions.scalaAll,
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % Versions.macroCompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers" %% "phantom-dsl" % Versions.phantom,
      "com.outworkers" %% "util-testing" % Versions.util % Test
    )
  ).enablePlugins(CrossPerProjectPlugin)


lazy val phantomMonix = (project in file("phantom-monix"))
  .settings(
    name := "phantom-monix",
    crossScalaVersions := Versions.scalaAll,
    moduleName := "phantom-monix",
    libraryDependencies ++= Seq(
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "io.monix" %% "monix" % Versions.monix
    )
  ).settings(
    sharedSettings: _*
  )