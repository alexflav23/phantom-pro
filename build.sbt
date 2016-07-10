import sbt._
import Keys._
import com.twitter.sbt.{GitProject, VersionManagement}

lazy val Versions = new {
  val phantom = "1.27.0"
  val util = "0.18.2"
  val datastax = "3.0.2"
  val dse = "3.0.0-rc1"
  val scalaTest = "2.2.4"
  val shapeless = "2.3.1"
  val scalaMeter = "0.7"
  val spark = "1.5.0-M2"
  val scalamock = "3.2.2"
}

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  version := Versions.phantom,
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  fork in Test := true,
  testOptions in Test += Tests.Argument("-oF"),
  logBuffered in Test := false,
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, 4)
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("staging"),
    Resolver.typesafeRepo("releases"),
    Resolver.typesafeRepo("snapshots"),
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.bintrayRepo("websudos", "oss-releases")
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-Yinline-warnings",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  fork in Test := true,
  javaOptions in Test ++= Seq("-Xmx2G")
) ++ graphSettings ++ GitProject.gitSettings ++ VersionManagement.newSettings

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
    phantomMigrations,
    phantomSpark,
    phantomUdt,
    phantomAutoTables
  )

lazy val phantomDse = (project in file("phantom-dse"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-dse",
    libraryDependencies ++= Seq(
      "com.websudos" 								 %% "phantom-dsl" 										 % Versions.phantom,
      "com.datastax.cassandra"       %  "cassandra-driver-dse"             % Versions.dse,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    )
  )

lazy val phantomMigrations = (project in file("phantom-migrations"))
  .settings(sharedSettings: _* )
  .settings(
    moduleName := "phantom-migrations",
    libraryDependencies ++= Seq(
      "com.websudos" 								 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"                 %% "util-testing"                     % Versions.util % Test
    )
  )

lazy val phantomSpark = (project in file("phantom-spark"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-spark",
    libraryDependencies ++= Seq(
      "com.datastax.spark"           %% "spark-cassandra-connector"        % Versions.spark,
      "com.websudos" 								 %% "phantom-dsl" 										 % Versions.phantom
    )
  )

lazy val phantomAutoTables = (project in file("phantom-autotables"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-autotables",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile).value / ("scala-2." + {
        if(scalaBinaryVersion.value.startsWith("2.10")) "10" else "11"
    })),
    libraryDependencies ++= Seq(
      "org.scala-lang"               %  "scala-reflect"                    % scalaVersion.value,
      "com.websudos" 								 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test,
      "org.scalamock"                %% "scalamock-scalatest-support"      % Versions.scalamock % Test
    )
  )

lazy val phantomUdt = (project in file("phantom-udt"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-udt",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile).value / ("scala-2." + {
        if(scalaBinaryVersion.value.startsWith("2.10")) "10" else "11"
    })),
    libraryDependencies ++= Seq(
      "com.websudos" 								 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test,
      "org.scalamock"                %% "scalamock-scalatest-support"      % Versions.scalamock % Test
    )
  )