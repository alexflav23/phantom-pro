/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 10/2017.
 */
package com.outworkers.phantom.monix.execution

import com.outworkers.phantom.builder.query.execution.{FutureMonad, PromiseInterface}
import monix.eval.Task

import scala.concurrent.ExecutionContextExecutor

object MonixImplicits {

  implicit val taskInterface: PromiseInterface[Task, Task] = new MonixPromiseInterface

  implicit val taskMonad: FutureMonad[Task] = new FutureMonad[Task] {

    override def flatMap[A, B](source: Task[A])(fn: (A) => Task[B])(
      implicit ctx: ExecutionContextExecutor
    ): Task[B] = source flatMap fn

    override def map[A, B](source: Task[A])(f: (A) => B)(
      implicit ctx: ExecutionContextExecutor
    ): Task[B] = source map f

    override def pure[A](source: A): Task[A] = Task.pure(source)
  }

}
