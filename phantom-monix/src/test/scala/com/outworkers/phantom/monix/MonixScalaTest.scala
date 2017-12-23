/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.monix

import monix.eval.Task
import org.scalatest.concurrent.{ScalaFutures, Waiters}

import scala.util.{Failure, Success}
import _root_.monix.execution.Scheduler.Implicits.global

trait MonixScalaTest extends Waiters with ScalaFutures {

  implicit def taskFutureConcept[T](f: Task[T]): FutureConcept[T] = new FutureConcept[T] {

    private[this] val source = f.memoize

    override def eitherValue: Option[Either[Throwable, T]] = {
      source.runAsync.value match {
        case Some(Success(ret)) => Some(Right(ret))
        case Some(Failure(err)) => Some(Left(err))
        case None => None
      }
    }

    override def isExpired: Boolean = false

    override def isCanceled: Boolean = false
  }
}
