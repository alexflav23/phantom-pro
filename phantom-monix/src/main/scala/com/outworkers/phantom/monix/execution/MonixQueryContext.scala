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
  override def await[T](f: Task[T], timeout: Duration): T = {
    Await.result(f.runAsync, timeout)
  }
}
