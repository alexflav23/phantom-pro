/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.docker

import java.util.concurrent.Executors

import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerManager, DockerContainerState, DockerFactory}
import sbt.Logger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class DockerSbtKit(
  factory: DockerFactory,
  logger: Logger,
  dockerContainers: List[DockerContainer],
  pullImagesTimeout: Duration,
  startContainerTimeout: Duration,
  stopContainer: Duration
) {

  implicit def dockerFactory: DockerFactory = factory

  // we need ExecutionContext in order to run docker.init() / docker.stop() there
  implicit lazy val dockerExecutionContext: ExecutionContext = {
    // using Math.max to prevent unexpected zero length of docker containers
    ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(Math.max(1, dockerContainers.length * 2))
    )
  }
  implicit lazy val dockerExecutor: DockerCommandExecutor = dockerFactory.createExecutor()

  lazy val containerManager = new DockerContainerManager(dockerContainers, dockerExecutor)

  def isContainerReady(container: DockerContainer): Future[Boolean] =
    containerManager.isReady(container)

  def getContainerState(container: DockerContainer): DockerContainerState = {
    containerManager.getContainerState(container)
  }

  implicit def containerToState(c: DockerContainer): DockerContainerState = {
    getContainerState(c)
  }


  def timed[T](f: => T): T = {
    val start = System.nanoTime()
    val x = f
    val end = System.nanoTime()
    val time = (end - start).nanos
    logger.info(s"Callback completed in ${time.toMillis} milliseconds")
    x
  }

  def waitForReadyChecks(duration: Duration): Unit = {
    logger.info("Waiting for all ready states to be complete")

    val futures = dockerContainers.map(container => container.readyChecker(getContainerState(container)))

    timed {
      Await.result(Future.sequence(futures), duration)
    }
  }

  def startAllOrFail(): Unit = {
    Await.result(containerManager.pullImages(), pullImagesTimeout)

    val allRunning: Boolean = try {
      val future: Future[Boolean] =
        containerManager.initReadyAll(startContainerTimeout).map(x => {
          logger.info("Finished initialising containers using containerManager.")
          logger.info(s"Container states: ${x.mkString(", ")}")
          x.map(_._2).forall(true ==)
        })

      sys.addShutdownHook(
        containerManager.stopRmAll()
      )

      Await.result(future, startContainerTimeout)

    } catch {
      case e: Exception =>
        logger.error("Exception during container initialization")
        logger.trace(e)
        false
    }
    if (!allRunning) {
      Await.result(containerManager.stopRmAll(), stopContainer)
      throw new RuntimeException("Cannot run all required containers")
    }
  }

  def stopAllQuietly[T](f: => T): Unit = {
    try {
      Await.result(containerManager.stopRmAll() map ( _ => { val x = f }), stopContainer)
    } catch {
      case e: Throwable =>
        logger.error(e.getMessage)
        logger.trace(e)
    }
  }


}
