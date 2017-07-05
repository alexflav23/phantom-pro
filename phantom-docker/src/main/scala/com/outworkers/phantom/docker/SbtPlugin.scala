/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.docker

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.spotify.docker.client.DefaultDockerClient
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.{DockerContainer, DockerReadyChecker}
import org.slf4j.LoggerFactory
import sbt.Keys._
import sbt._

import scala.concurrent.duration._

class SbtPlugin extends AutoPlugin {

  override def requires: Plugins = sbt.plugins.JvmPlugin

  val logger = LoggerFactory.getLogger(this.getClass)

  final val defaultCqlPort: Int = 9042

  final val defaultPullImagesTimeout: Duration = 20.minutes
  final val defaultStartContainersTimeout: Duration = 1.minute
  final val defaultStopContainersTimeout: Duration = 1.minute

  val state = new AtomicBoolean(false)
  val dockerKitRef = new AtomicReference[Option[DockerSbtKit]](None)

  val defaultCassandraContainer = DockerContainer("cassandra:3.11")

  def initCassandraContainer(
    cassandraPort: Int,
    cassandraDockerImage: DockerContainer,
    pullTimeout: Duration,
    startTimeout: Duration,
    stopTimeout: Duration
  ): Unit = {
    logger.info(s"Using Docker image ${cassandraDockerImage.image} to launch Cassandra")

    val cassandraContainer = cassandraDockerImage
      .withPorts(cassandraPort -> Some(cassandraPort))
      .withReadyChecker(DockerReadyChecker.LogLineContains("Starting listening for CQL clients on"))

    val dockerKit = new DockerSbtKit(
      new SpotifyDockerFactory(DefaultDockerClient.fromEnv().build()),
      cassandraContainer :: Nil,
      pullTimeout,
      startTimeout,
      stopTimeout
    )

    dockerKitRef.set(Some(dockerKit))

    logger.info("Starting docker container")

    logger.info("Successfully started Cassandra Docker container")

    dockerKit.startAllOrFail()

  }

  /**
    * Keys for all settings of this plugin.
    */
  object autoImport {

    val cassandraPort = taskKey[Option[Int]]("The default CQL port to use. This will be exposed from within Docker.")
    val cassandraDockerImage = taskKey[Option[String]]("Cassandra image to use as source for the docker repository")
    val dockerStartTimeoutSeconds = taskKey[Option[Long]]("Seconds to wait for Docker containers to startup up")
    val dockerStopTimeoutSeconds = taskKey[Option[Long]]("Seconds to wait for Docker containers to shut down")
    val dockerPullTimeoutMinutes = taskKey[Option[Int]]("Minutes to wait for docker images to be resolved")
    val startCassandraContainer = taskKey[Unit]("Starts up a Cassandra cluster in Docker")
    val stopCassandraContainer = taskKey[Unit]("Stop the Cassandra Docker container")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    logBuffered := true,
    stopCassandraContainer := {
      val ref = dockerKitRef.get()
      if (ref.isEmpty) {
        throw new IllegalStateException("There appears to be no DockerKit created within this runtime, this shouldn't happen")
      } else {
        ref.get.stopAllQuietly()
        logger.info("Successfully stopped all Docker containers")
      }
    },
    test in Test := (test in Test).dependsOn(startCassandraContainer),
    testQuick in Test := (testQuick in Test).dependsOn(startCassandraContainer),
    testOnly in Test := (testOnly in Test).dependsOn(startCassandraContainer),

    testOptions in Test += Tests.Cleanup(() => dockerKitRef.get().foreach(_.stopAllQuietly())),
    startCassandraContainer := {
      val realPort = cassandraPort.value.getOrElse(defaultCqlPort)
      logger.info(s"Using port $realPort for Cassandra CQL interface. Automatically exposing this from Docker")

      val cassandraBaseImage = cassandraDockerImage.value.map(DockerContainer(_)).getOrElse(defaultCassandraContainer)
      logger.info(s"Using Docker image ${cassandraBaseImage.image} to launch Cassandra")

      val pullTimeout = dockerPullTimeoutMinutes.value.map(_.minutes).getOrElse(defaultPullImagesTimeout)
      logger.info(s"Waiting for $pullTimeout for Docker dependencies to resolve")

      val startTimeout = dockerStartTimeoutSeconds.value.map(_.seconds).getOrElse(defaultStartContainersTimeout)
      logger.info(s"Waiting for $startTimeout for docker containers to start")

      val stopTimeout = dockerStopTimeoutSeconds.value.map(_.seconds).getOrElse(defaultStopContainersTimeout)
      logger.info(s"Will wait for $stopTimeout for docker contains to stop")

      if (state.compareAndSet(false, true)) {
        logger.info("Cassandra container not running, attempting to start Docker container")

        initCassandraContainer(
          cassandraPort = realPort,
          cassandraDockerImage = cassandraBaseImage,
          pullTimeout = pullTimeout,
          startTimeout = startTimeout,
          stopTimeout = stopTimeout
        )
      } else {
        logger.info("Cassandra container is already running, not attempting to start it again")
      }

    },
    fork := true
  )
}
