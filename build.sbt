import sbt._
import Keys._
import com.twitter.sbt.{GitProject, VersionManagement}

lazy val Versions = new {
  val phantom = "2.0.3"
  val util = "0.23.1"
  val dse = "1.1.0"
  val scalaTest = "2.2.4"
  val shapeless = "2.3.1"
  val scalaMeter = "0.7"
  val spark = "1.6.0"
  val dseDriver = "1.1.0"
}

val scalaMacroDependencies: String => Seq[ModuleID] = {
  s => CrossVersion.partialVersion(s) match {
    case Some((major, minor)) if minor >= 11 => Seq.empty
    case _ => Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
  }
}

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  fork in Test := true,
  testOptions in Test += Tests.Argument("-oF"),
  logBuffered in Test := false,
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, 4)
  ),
  gitTagName <<= (organization, name, version) map { (o, n, v) =>
    "version=%s".format(v)
  },
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Twitter Repository" at "http://maven.twttr.com",
    Resolver.bintrayRepo("outworkers", "oss-releases"),
    Resolver.bintrayRepo("outworkers", "internal-releases"),
    Resolver.bintrayRepo("outworkers", "enterprise-releases")
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-language:experimental.macros",
    "-Yinline-warnings",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  fork in Test := true,
  javaOptions in Test ++= Seq("-Xmx2G")
) ++ GitProject.gitSettings ++ VersionManagement.newSettings ++ Publishing.effectiveSettings

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
    phantomSpark,
    phantomUdt,
    phantomAutoTables
  )

lazy val phantomDse = (project in file("phantom-dse"))
  .settings(sharedSettings: _*)
  .settings(
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
    moduleName := "phantom-migrations",
    libraryDependencies ++= Seq(
      "com.outworkers" 								 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"                 %% "util-testing"                     % Versions.util % Test
    )
  )

lazy val phantomSpark = (project in file("phantom-spark"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-spark",
    libraryDependencies ++= Seq(
      "com.datastax.spark"           %% "spark-cassandra-connector"        % Versions.spark,
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom
    )
  )

lazy val phantomAutoTables = (project in file("phantom-autotables"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-autotables",
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile).value / ("scala-2." + {
        if (scalaBinaryVersion.value.startsWith("2.10")) "10" else "11"
    })),
    libraryDependencies ++= Seq(
      "com.outworkers" 							%% "phantom-dsl" 										   % Versions.phantom,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    ) ++ scalaMacroDependencies(scalaVersion.value)
  )

lazy val phantomDseGraph = (project in file("phantom-graph"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-graph",
    libraryDependencies ++= Seq(
      "com.datastax.cassandra"       % "dse-driver"                        % Versions.dseDriver,
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"               %% "util-testing"                     % Versions.util % Test
    ) ++ scalaMacroDependencies(scalaVersion.value)
  )

lazy val phantomUdt = (project in file("phantom-udt"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-udt",
    scalacOptions ++= Seq(
      //"-Ymacro-debug-verbose",
      //"-Yshow-trees-stringified"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      "org.typelevel"  %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers" %% "phantom-dsl" % Versions.phantom,
      "com.outworkers" %% "util-testing" % Versions.util % Test
    )
  )