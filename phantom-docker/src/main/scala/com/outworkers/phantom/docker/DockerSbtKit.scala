/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.docker

import java.util.concurrent.Executors

import com.whisk.docker.{DockerContainer, DockerContainerManager, DockerContainerState, DockerFactory}
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext, Future}

class DockerSbtKit(
  factory: DockerFactory,
  dockerContainers: List[DockerContainer],
  pullImagesTimeout: Duration,
  startContainerTimeout: Duration,
  stopContainer: Duration
) {

  implicit def dockerFactory: DockerFactory = factory

  private lazy val log = LoggerFactory.getLogger(this.getClass)

  // we need ExecutionContext in order to run docker.init() / docker.stop() there
  implicit lazy val dockerExecutionContext: ExecutionContext = {
    // using Math.max to prevent unexpected zero length of docker containers
    ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(Math.max(1, dockerContainers.length * 2))
    )
  }
  implicit lazy val dockerExecutor = dockerFactory.createExecutor()

  lazy val containerManager = new DockerContainerManager(dockerContainers, dockerExecutor)

  def isContainerReady(container: DockerContainer): Future[Boolean] =
    containerManager.isReady(container)

  def getContainerState(container: DockerContainer): DockerContainerState = {
    containerManager.getContainerState(container)
  }

  implicit def containerToState(c: DockerContainer): DockerContainerState = {
    getContainerState(c)
  }

  def startAllOrFail(): Unit = {
    Await.result(containerManager.pullImages(), pullImagesTimeout)

    val allRunning: Boolean = try {
      val future: Future[Boolean] =
        containerManager.initReadyAll(startContainerTimeout).map(_.map(_._2).forall(identity))
      sys.addShutdownHook(
        containerManager.stopRmAll()
      )
      Await.result(future, startContainerTimeout)
    } catch {
      case e: Exception =>
        log.error("Exception during container initialization", e)
        false
    }
    if (!allRunning) {
      Await.result(containerManager.stopRmAll(), stopContainer)
      throw new RuntimeException("Cannot run all required containers")
    }
  }

  def stopAllQuietly(): Unit = {
    try {
      Await.result(containerManager.stopRmAll(), stopContainer)
    } catch {
      case e: Throwable =>
        log.error(e.getMessage, e)
    }
  }


}
