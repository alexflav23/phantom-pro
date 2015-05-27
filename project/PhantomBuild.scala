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

import com.twitter.sbt.GitProject
import com.twitter.sbt.VersionManagement
import com.twitter.scrooge.ScroogeSBT
import sbt.Keys._
import sbt._

object PhantomBuild extends Build {

  val UtilVersion = "0.9.6"
  val PhantomVersion = "1.9.1"
  val DatastaxDriverVersion = "2.1.5"
  val ScalaTestVersion = "2.2.4"
  val ShapelessVersion = "2.2.0-RC4"
  val Json4SVersion = "3.2.11"
  val ScalaMeterVersion = "0.6"
  val CassandraUnitVersion = "2.1.3.2"
  val SparkCassandraVersion = "1.2.0-alpha3"

  val PerformanceTest = config("perf").extend(Test)
  def performanceFilter(name: String): Boolean = name endsWith "PerformanceTest"

  val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.websudos",
    version := "1.9.1",
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.10.5", "2.11.6"),
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
    javaOptions in Test ++= Seq("-Xmx2G"),
    testFrameworks in PerformanceTest := Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    testOptions in Test := Seq(Tests.Filter(x => !performanceFilter(x))),
    testOptions in PerformanceTest := Seq(Tests.Filter(x => performanceFilter(x))),
    fork in PerformanceTest := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ GitProject.gitSettings ++ VersionManagement.newSettings


  lazy val phantomEnterprise = Project(
    id = "phantom-enterprise",
    base = file("."),
    settings = sharedSettings
  ).configs(
    PerformanceTest
  ).settings(
    name := "phantom-enterprise"
  ).aggregate(
    phantomDse,
    phantomSpark,
    phantomMigrations
  )

  lazy val phantomDse = Project(
  	id = "phantom-dse",
  	base = file("phantom-dsl"),
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
			"com.websudos" 								 %% "phantom-testkit" 								 % PhantomVersion         % "test, provided",
  		"com.websudos"                 %% "util-testing"                     % UtilVersion            % "test, provided"
		)		
	
	).dependsOn(
		phantomDse
	)

	lazy val phantomSpark = Project(
		id = "phantom-spark",
		base = file("phantom-spark"),
		settings = Defaults.coreDefaultSettings ++ sharedSettings
	).settings(
		libraryDependencies ++= Seq(
			"com.datastax.spark"           %% "spark-cassandra-connector"        % SparkCassandraVersion,
			"com.websudos" 								 %% "phantom-dsl" 										 % PhantomVersion,
			"com.websudos" 								 %% "phantom-testkit" 								 % PhantomVersion % "test, provided"
		)
	)
}
