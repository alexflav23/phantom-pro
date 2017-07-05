/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import bintray.BintrayKeys._
import sbt.Keys._
import sbt._

import scala.util.Properties

object Publishing {

  val defaultPublishingSettings = Seq(
    version := "0.6.0"
  )

  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
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
    pomIncludeRepository := { _ => true},
    licenses += (
      "Apache-2.0",
      url("https://github.com/outworkers/phantom/blob/develop/LICENSE.txt")
    )
  ) ++ defaultPublishingSettings

  def effectiveSettings: Seq[Def.Setting[_]] = bintraySettings

  def runningUnderCi: Boolean = sys.env.get("CI").isDefined || sys.env.get("TRAVIS").isDefined
  def travisScala211: Boolean = sys.env.get("TRAVIS_SCALA_VERSION").exists(_.contains("2.11"))

  def isTravisScala210: Boolean = !travisScala211

  def isJdk8: Boolean = sys.props("java.specification.version") == "1.8"

  lazy val addOnCondition: (Boolean, ProjectReference) => Seq[ProjectReference] = (bool, ref) =>
    if (bool) ref :: Nil else Nil

}
