/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 */

import com.twitter.sbt.{GitProject, VersionManagement}
import sbt.Keys._
import sbt._

object Build extends Build {

  val UtilVersion = "0.10.6"
  val PhantomVersion = "1.20.1"
  val DatastaxDriverVersion = "3.0.0-alpha4"
  val ScalaTestVersion = "2.2.4"
  val ShapelessVersion = "2.2.4"
  val ScalaMeterVersion = "0.6"
  val SparkCassandraVersion = "1.5.0-M2"

  val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.websudos",
    version := PhantomVersion,
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.10.5", "2.11.7"),
    resolvers ++= Seq(
      "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
      Resolver.bintrayRepo("websudos", "oss-releases")
    ),
    scalacOptions ++= Seq(
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
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ GitProject.gitSettings ++ VersionManagement.newSettings


  lazy val phantomEnterprise = Project(
    id = "phantom-enterprise",
    base = file("."),
    settings = sharedSettings
  ).settings(
    name := "phantom-enterprise"
  ).aggregate(
    phantomAutoTables,
    phantomDse,
    phantomSpark,
    phantomMigrations
  )

  lazy val phantomDse = Project(
  	id = "phantom-dse",
  	base = file("phantom-dse"),
  	settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
   	fork := true,
    testOptions in Test += Tests.Argument("-oF"),
    logBuffered in Test := false,
    concurrentRestrictions in Test := Seq(
      Tags.limit(Tags.ForkedTestGroup, 4)
    ),
  	libraryDependencies ++= Seq(
  		"com.websudos" 								 %% "phantom-dsl" 										 % PhantomVersion,
  		"com.datastax.cassandra"       %  "cassandra-driver-dse"             % DatastaxDriverVersion,
  		"com.websudos"                 %% "util-testing"                     % UtilVersion            % "test, provided"
  	)
	)

	lazy val phantomMigrations = Project(
		id = "phantom-migrations",
		base = file("phantom-migrations"),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).settings(
		libraryDependencies ++= Seq(
			"com.websudos" 								 %% "phantom-dsl" 										 % PhantomVersion,
  		"com.websudos"                 %% "util-testing"                     % UtilVersion            % "test, provided"
		)		
	
	).dependsOn(
		phantomDse
	)

  lazy val phantomAutoTables = Project(
    id = "phantom-autotables",
    base = file("phantom-autotables"),
    settings = Defaults.coreDefaultSettings ++ sharedSettings
  ).settings(
    scalacOptions ++= Seq(
      "-language:experimental.macros"
    ),
  libraryDependencies ++= Seq(
    "org.scala-lang"               %  "scala-reflect"                    % scalaVersion.value,
    "com.websudos" 								 %% "phantom-dsl" 										 % PhantomVersion,
    "com.websudos"                 %% "util-testing"                     % UtilVersion            % "test, provided"
    ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
  unmanagedSourceDirectories in Compile ++= Seq(
    (sourceDirectory in Compile).value / ("scala-2." + (if(scalaBinaryVersion.value.startsWith("2.10")) "10" else "11")))

  )

	lazy val phantomSpark = Project(
		id = "phantom-spark",
		base = file("phantom-spark"),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).settings(
		libraryDependencies ++= Seq(
			"com.datastax.spark"           %% "spark-cassandra-connector"        % SparkCassandraVersion,
			"com.websudos" 								 %% "phantom-dsl" 										 % PhantomVersion
		)
	)
}
