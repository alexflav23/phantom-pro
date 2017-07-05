import sbt._
import Keys._
import com.twitter.sbt.{GitProject, VersionManagement}

lazy val Versions = new {
  val phantom = "2.12.1"
  val util = "0.36.0"
  val logback = "1.2.1"
  val dse = "1.1.0"
  val scalaTest = "3.0.1"
  val shapeless = "2.3.2"
  val scalaMeter = "0.8.3"
  val spark = "1.6.0"
  val dseDriver = "1.1.0"
  val macroCompat = "1.1.1"
  val macroParadise = "2.1.0"
  val scalaGraph = "1.11.4"
  val dockerKit = "0.9.0"
}

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := "2.11.8",
  fork in Test := true,
  testOptions in Test += Tests.Argument("-oF"),
  logBuffered in Test := false,
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, 4)
  ),
  gitTagName := s"version=${scalaVersion.value}",
  libraryDependencies ++= Seq(
     "ch.qos.logback" % "logback-classic" % Versions.logback % Test
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
) ++ GitProject.gitSettings ++
  VersionManagement.newSettings ++
  Publishing.effectiveSettings

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
  )

lazy val phantomDse = (project in file("phantom-dse"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
    moduleName := "phantom-dse",
    libraryDependencies ++= Seq(
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.datastax.cassandra"       %  "dse-driver"                       % Versions.dse,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    )
  )

lazy val phantomMigrations = (project in file("phantom-migrations"))
  .settings(sharedSettings: _* )
  .settings(
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
    moduleName := "phantom-migrations",
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % Versions.macroCompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers" %% "phantom-dsl" % Versions.phantom,
      "com.outworkers"  %% "util-testing" % Versions.util % Test
    )
  )

lazy val phantomSpark = (project in file("phantom-spark"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    moduleName := "phantom-spark",
    libraryDependencies ++= Seq(
      "com.datastax.spark"           %% "spark-cassandra-connector"        % Versions.spark,
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom
    )
  )

lazy val phantomAutoTables = (project in file("phantom-autotables"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
    moduleName := "phantom-autotables",
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "com.outworkers" 							%% "phantom-dsl" 										  % Versions.phantom,
      "com.outworkers"              %% "util-testing"                     % Versions.util % Test
    )
  )

lazy val phantomDseGraph = (project in file("phantom-graph"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-graph",
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       % "dse-driver"                        % Versions.dseDriver,
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    )
  )

lazy val phantomDocker = (project in file("phantom-docker"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-udt",
    publishMavenStyle := false,
    sbtPlugin := true,
    crossScalaVersions := Seq("2.10.6"),
    libraryDependencies ++= Seq(
      "com.whisk" %% "docker-testkit-scalatest" % Versions.dockerKit,
      "com.whisk" %% "docker-testkit-impl-spotify" % Versions.dockerKit
    )
  )

lazy val phantomUdt = (project in file("phantom-udt"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-udt",
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % Versions.macroCompat,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers" %% "phantom-dsl" % Versions.phantom,
      "com.outworkers" %% "util-testing" % Versions.util % Test
    )
  )
