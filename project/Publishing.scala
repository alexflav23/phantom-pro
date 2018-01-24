/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 10/2017.
 */
import bintray.BintrayKeys._
import com.typesafe.sbt.SbtGit.git
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, _}
import sbtrelease.ReleaseStateTransformations._

object Publishing {

  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )


  val ciSkipSequence = "[ci skip]"

  val releaseSettings = Seq(
    releaseIgnoreUntrackedFiles := true,
    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    releaseTagComment := s"Releasing ${(version in ThisBuild).value}",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} $ciSkipSequence",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("such publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  lazy val defaultCredentials: Seq[Credentials] = {
    if (!Publishing.runningUnderCi) {
      Seq(
        Credentials(Path.userHome / ".bintray" / ".credentials"),
        Credentials(Path.userHome / ".ivy2" / ".credentials")
      )
    } else {
      Seq(
        Credentials(
          realm = "Bintray",
          host = "dl.bintray.com",
          userName = System.getenv("bintray_user"),
          passwd = System.getenv("bintray_password")
        ),
        Credentials(
          realm = "Bintray API Realm",
          host = "api.bintray.com",
          userName = System.getenv("bintray_user"),
          passwd = System.getenv("bintray_password")
        )
      )
    }
  }

  lazy val bintraySettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintrayOrganization := Some("outworkers"),
    bintrayRepository := {
      if (scalaVersion.value.trim.endsWith("SNAPSHOT")) {
        "enterprise-snapshots"
      } else {
        "enterprise-releases"
      }
    },
    bintrayReleaseOnPublish in ThisBuild := true,
    publishArtifact in Test := false,
    publishArtifact in (Compile, packageSrc) := false,
    publishArtifact in (Test, packageSrc) := false,
    pomIncludeRepository := { _ => true}
  )

  def effectiveSettings: Seq[Def.Setting[_]] = bintraySettings ++ releaseSettings

  def runningUnderCi: Boolean = sys.env.get("CI").isDefined || sys.env.get("TRAVIS").isDefined
  def travisScala211: Boolean = sys.env.get("TRAVIS_SCALA_VERSION").exists(_.contains("2.11"))

  def isTravisScala210: Boolean = !travisScala211

  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  lazy val addOnCondition: (Boolean, ProjectReference) => Seq[ProjectReference] = (bool, ref) =>
    if (bool) ref :: Nil else Nil

}
