package com.outworkers.phantom.monix.execution

import com.datastax.driver.core.{Session, Statement}
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.execution.{FutureMonad, GuavaAdapter, PromiseInterface}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import monix.eval.Task
import monix.execution.Cancelable

import scala.concurrent.{ExecutionContextExecutor, Promise}

class MonixPromiseInterface extends PromiseInterface[Task, Task]{
  override def empty[T]: Task[T] = Task.fromFuture(Promise.apply[T].future)

  override def apply[T](value: T): Task[T] = Task.now(value)

  override def become[T](source: Task[T], value: Task[T]): Task[T] = {
    Task.raiseError(new Exception("This method call has been hit: become"))
  }

  override def adapter(
    implicit monad: FutureMonad[Task]
  ): GuavaAdapter[Task] = new GuavaAdapter[Task] with SessionAugmenterImplicits {
    override def fromGuava[T](source: ListenableFuture[T])(
      implicit executor: ExecutionContextExecutor
    ): Task[T] = {
      Task.create[T] { (s, cb) =>
        val callback = new FutureCallback[T] {
          def onSuccess(result: T): Unit = {
            cb.onSuccess(result)
          }

          def onFailure(err: Throwable): Unit = {
            cb.onError(err)
          }
        }

        Futures.addCallback(source, callback, executor)
        Cancelable.empty
      }
    }

    override def fromGuava(in: Statement)(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): Task[ResultSet] = {
      fromGuava(session.executeAsync(in)).map(res => ResultSet(res, session.protocolVersion))
    }
  }

  override def future[T](source: Task[T]): Task[T] = source

  override def failed[T](exception: Throwable): Task[T] = Task.raiseError(exception)
}
