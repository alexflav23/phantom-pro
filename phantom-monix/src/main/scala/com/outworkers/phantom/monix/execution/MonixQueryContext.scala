/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 10/2017.
 */
package com.outworkers.phantom.monix.execution

import com.outworkers.phantom.ops.QueryContext
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration._

class MonixQueryContext()(implicit scheduler: Scheduler) extends QueryContext[Task, Task, Duration](10.seconds)(
  MonixImplicits.taskMonad,
  MonixImplicits.taskInterface
) {
  override def blockAwait[T](f: Task[T], timeout: Duration): T = {
    Await.result(f.runAsync, timeout)
  }
}
