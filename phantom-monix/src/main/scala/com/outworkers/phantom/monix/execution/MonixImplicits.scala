
package com.outworkers.phantom.monix.execution

import com.outworkers.phantom.builder.query.execution.{FutureMonad, GuavaAdapter, PromiseInterface}
import monix.eval.Task
import com.outworkers.phantom.dsl.{ promiseInterface, futureMonad }

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
