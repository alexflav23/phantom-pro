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
