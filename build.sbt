
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val Versions = new {
  val phantom = "2.59.0"
  val util = "0.57.0"
  val logback = "1.2.3"
  val dse = "2.4.0"
  val scalaTest = "3.1.1"
  val scalaTestPlus = "3.1.0.0-RC2"
  val macroParadise = "2.1.1"
  val scala211 = "2.11.12"
  val scala212 = "2.12.10"
  val scala213 = "2.13.1"
  val scalaAll = Seq(scala211, scala212, scala213)

  val catsScalaTestVersion: String => String = { v =>
    CrossVersion.partialVersion(v) match {
      case Some((_, minor)) if minor >= 12 => "3.0.5"
      case Some((_, minor)) if minor >= 11 => "3.0.0"
    }
  }


  val macroCompatVersion: String => ModuleID = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 13 => "org.typelevel" % "macro-compat_2.13.0-RC2" % "1.1.1"
      case Some((_, minor)) if minor < 13 => "org.typelevel" %% "macro-compat" % "1.1.1"
    }
  }

  val scalaMacrosVersion: String => String = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 11 => macroParadise
      case _ => "2.1.0"
    }
  }

  val paradiseVersion: String => Seq[ModuleID] = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 13 =>
        Nil
      case Some((_, minor)) if minor < 13 =>
        List(
          compilerPlugin("org.scalamacros" % "paradise" % scalaMacrosVersion(s) cross CrossVersion.full)
        )
    }
  }

  val catsVersion: String => ModuleID = {
    s => CrossVersion.partialVersion(s) match {
      case Some((_, minor)) if minor >= 13 => "org.typelevel" %% "cats-core" % "2.0.0"
      case Some((_, minor)) if minor >= 11 => "org.typelevel" %% "cats-core" % "1.6.0"
      case _ => "org.typelevel" %% "cats-core" % "1.2.0"
    }
  }
}

lazy val ScalacOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-feature",
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  //"-Xfatal-warnings", // Fail the compilation if there are any warnings
  //"-Yno-adapted-args" // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
)

val XLintOptions = Seq(
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match" // Pattern match may not be typesafe.
)

val Scala212Options = Seq(
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ypartial-unification", // Enable partial unification in type constructor inference,
  //"-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
  //"-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Xfuture" // Turn on future language features.
) ++ XLintOptions

val YWarnOptions = Seq(
  "-Ywarn-dead-code", // Warn when dead code is identified.
  //"-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  //"-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  //"-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

val scalacOptionsFn: String => Seq[String] = { s =>
  CrossVersion.partialVersion(s) match {
    case Some((_, minor)) if minor >= 13 => ScalacOptions ++ YWarnOptions ++ Seq("-Ymacro-annotations")
    case Some((_, minor)) if minor >= 12 => ScalacOptions ++ Scala212Options ++ YWarnOptions
    case _ => ScalacOptions ++ YWarnOptions
  }
}

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := Versions.scala213,
  fork in Test := true,
  testOptions in Test += Tests.Argument("-oF"),
  logBuffered in Test := false,
  concurrentRestrictions in Test := Seq(
    Tags.limit(Tags.ForkedTestGroup, 4)
  ),
  libraryDependencies ++= Seq(
     "ch.qos.logback" % "logback-classic" % Versions.logback % Test
  ),
  scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Java.net Maven2 Repository" at "https://download.java.net/maven/2/",
    "Twitter Repository" at "https://maven.twttr.com",
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
  javaOptions in Test ++= Seq(
    "-Xmx2G",
    "-Djava.net.preferIPv4Stack=true",
    "-Dio.netty.resourceLeakDetection"
  )
)

githubOwner := "outworkers"
githubRepository := "phantom"

lazy val phantomPro = (project in file("."))
  .settings(sharedSettings: _*)
  .settings(Publishing.noPublishSettings: _*)
  .settings(
    moduleName := "phantom-pro",
    scalacOptions := scalacOptionsFn(scalaVersion.value)
  ).aggregate(
    phantomDse,
    phantomDseGraph,
    phantomMigrations,
    phantomUdt,
    phantomAutoTables
    //phantomDocker
  )

lazy val phantomDse = (project in file("phantom-dse"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Versions.scalaAll,
    scalacOptions := scalacOptionsFn(scalaVersion.value),
    moduleName := "phantom-dse",
    libraryDependencies ++= Seq(
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.datastax.dse"             %  "dse-java-driver-core"             % Versions.dse,
      "com.outworkers"               %% "util-samplers"                    % Versions.util % Test,
      "org.scalatest"                %% "scalatest"                        % Versions.scalaTest % Test,
      "org.scalatestplus"            %% "scalatestplus-scalacheck"         % Versions.scalaTestPlus % Test
    )
  )

lazy val phantomMigrations = (project in file("phantom-migrations"))
  .settings(sharedSettings: _* )
  .settings(
    crossScalaVersions := Versions.scalaAll,
    scalacOptions := scalacOptionsFn(scalaVersion.value),
    moduleName := "phantom-migrations",
    libraryDependencies ++= Seq(
      Versions.catsVersion(scalaVersion.value),
      Versions.macroCompatVersion(scalaVersion.value),
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers" %% "phantom-dsl" % Versions.phantom,
      "com.outworkers"  %% "util-samplers" % Versions.util % Test,
      "com.ironcorelabs" %% "cats-scalatest" % Versions.catsScalaTestVersion(scalaVersion.value) % Test,
      "org.scalatest"                %% "scalatest"                        % Versions.scalaTest % Test,
      "org.scalatestplus"            %% "scalatestplus-scalacheck"         % Versions.scalaTestPlus % Test
    ) ++ Versions.paradiseVersion(scalaVersion.value)
  )

lazy val phantomAutoTables = (project in file("phantom-autotables"))
  .settings(sharedSettings: _*)
  .settings(
    crossScalaVersions := Versions.scalaAll,
    scalacOptions := scalacOptionsFn(scalaVersion.value),
    moduleName := "phantom-autotables",
    libraryDependencies ++= Seq(
      "com.outworkers" 							%% "phantom-dsl" 										  % Versions.phantom,
      "com.outworkers"              %% "util-samplers"                    % Versions.util % Test,
      "org.scalatest"               %% "scalatest"                        % Versions.scalaTest % Test,
      "org.scalatestplus"           %% "scalatestplus-scalacheck"         % Versions.scalaTestPlus % Test
    ) ++ Versions.paradiseVersion(scalaVersion.value)
  )

lazy val phantomDseGraph = (project in file("phantom-graph"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-graph",
    crossScalaVersions := Versions.scalaAll,
    scalacOptions := scalacOptionsFn(scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.datastax.dse"             %  "dse-java-driver-core"             % Versions.dse,
      "com.outworkers" 							 %% "phantom-dsl" 										 % Versions.phantom,
      "com.outworkers"               %% "util-samplers"                    % Versions.util % Test,
      "org.scalatest"                %% "scalatest"                        % Versions.scalaTest % Test,
      "org.scalatestplus"            %% "scalatestplus-scalacheck"         % Versions.scalaTestPlus % Test
    )
  )

//lazy val phantomDocker = (project in file("phantom-docker"))
//  .settings(sharedSettings: _*)
//  .settings(
//    moduleName := "phantom-docker",
//    publishMavenStyle := false,
//    sbtPlugin := true,
//    publishArtifact in Test := false,
//    pomIncludeRepository := { _ => false },
//    crossScalaVersions := Seq(Versions.scala212),
//    libraryDependencies ++= Seq(
//      "com.whisk" %% "docker-testkit-scalatest" % Versions.dockerKit,
//      "com.whisk" %% "docker-testkit-impl-spotify" % Versions.dockerKit
//    )
//  )

lazy val phantomUdt = (project in file("phantom-udt"))
  .settings(sharedSettings: _*)
  .settings(
    moduleName := "phantom-udt",
    crossScalaVersions := Versions.scalaAll,
    scalacOptions := scalacOptionsFn(scalaVersion.value),
    libraryDependencies ++= Seq(
      Versions.macroCompatVersion(scalaVersion.value),
      "org.scala-lang"    % "scala-compiler" % scalaVersion.value % "provided",
      "com.outworkers"    %% "phantom-dsl" % Versions.phantom,
      "com.outworkers"    %% "util-samplers" % Versions.util % Test,
      "org.scalatest"     %% "scalatest"     % Versions.scalaTest % Test,
      "org.scalatestplus" %% "scalatestplus-scalacheck" % Versions.scalaTestPlus % Test
    ) ++ Versions.paradiseVersion(scalaVersion.value)
  )

lazy val readme = (project in file("readme"))
  .settings(sharedSettings)
  .settings(
    scalacOptions := scalacOptionsFn(scalaVersion.value),
    crossScalaVersions := Seq(Versions.scala211, Versions.scala212),
    tutSourceDirectory := sourceDirectory.value / "main" / "tut",
    tutTargetDirectory := phantomPro.base / "docs",
    libraryDependencies ++= Seq(
      Versions.macroCompatVersion(scalaVersion.value),
      "org.scala-lang"    % "scala-compiler" % scalaVersion.value,
      "com.outworkers"    %% "util-samplers" % Versions.util,
      "org.scalatest"     %% "scalatest" % Versions.scalaTest,
      "org.scalatestplus" %% "scalatestplus-scalacheck" % Versions.scalaTestPlus % Test
    ) ++ Versions.paradiseVersion(scalaVersion.value)
  ).dependsOn(
    phantomDse,
    phantomDseGraph,
    phantomMigrations,
    phantomUdt,
    phantomAutoTables
  ).enablePlugins(TutPlugin)
