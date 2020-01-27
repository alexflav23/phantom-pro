/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom

import cats.data.ValidatedNel
import cats.Semigroup
import cats.data.Validated.{Invalid, Valid}
import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.dsl.{context => _, _}
import com.outworkers.phantom.macros.DatabaseHelper
import com.outworkers.phantom.migrations.diffs.{DatabaseDiff, DiffConfig, DiffConflict, Differ}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.collection.compat._

package object migrations {

  implicit def auto[M[X] <: IterableOnce[X]]: Semigroup[QueryCollection[M]] = {
    new Semigroup[QueryCollection[M]] {
      override def combine(x: QueryCollection[M], y: QueryCollection[M]): QueryCollection[M] = x add y
    }
  }

  type MigrationResult[T] = ValidatedNel[DiffConflict, T]

  val defaultDuration: FiniteDuration = 20.seconds

  implicit class TableMigrations(val table: Table[_, _]) extends AnyVal {

    def automigrate()(
      implicit session: Session,
      keySpace: KeySpace,
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): MigrationResult[QueryCollection[Seq]] = {
      Differ.automigrate(table)
    }

    def automigrate(diffConfig: DiffConfig)(
      implicit session: Session,
      space: KeySpace,
      ec: ExecutionContextExecutor
    ): MigrationResult[QueryCollection[Seq]] = {
      Differ.automigrate(table)(session, space, ec, diffConfig)
    }
  }

  implicit class QueryColOps[M[X] <: IterableOnce[X]](val source: QueryCollection[M]) extends AnyVal {
    def add(other: QueryCollection[M]): QueryCollection[M] = source appendAll other.queries
  }

  implicit class DatabaseMigrations[DB <: Database[DB]](val db: DB) extends AnyVal {

    /**
      * Produces a final list of queries in the correct order than will need to be executed to resolve
      * all database diffs.
      * @param session The session in which to execute this operation.
      * @param space The keyspace in which the migration queries would execute.
      * @param ec The execution context in which to execute the queries.
      * @param diffConfig An implicit diff configuration.
      * @return An ordered list of dependent queries through [[QueryCollection[Seq]] that
      *         will resolve all conflicts with the schema in the database.
      */
    def automigrate()(
      implicit session: Session,
      space: KeySpace,
      helper: DatabaseHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): MigrationResult[DatabaseDiff[DB, DB]] = {
      import cats.syntax.validated._
      import cats.syntax.traverse._
      import cats.instances.list._

      helper.tables(db).map(Differ.automigrate).reduce(_ combine _).map(DatabaseDiff.apply)
    }


    /**
      * Asynchronously executes all migration queries in a one by one fashion.
      * @param session The session in which to execute this operation.
      * @param space The keyspace in which the migration queries would execute.
      * @param ec The execution context in which to execute the queries.
      * @param diffConfig An implicit diff configuration.
      * @return An ordered list of dependent queries through [[QueryCollection[Seq]] that
      *         will resolve all conflicts with the schema in the database.
      */
    def migrateAsyncNel()(
      implicit session: Session,
      space: KeySpace,
      helper: DatabaseHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): Future[MigrationResult[Seq[ResultSet]]] = {
      automigrate() fold(
        nel => Future.successful(Invalid(nel)),
        success => executeStatements(success.diffs).sequence() map (Valid(_))
      )
    }


    /**
      * Asynchronously executes all migration queries in a one by one fashion.
      * @param session The session in which to execute this operation.
      * @param space The keyspace in which the migration queries would execute.
      * @param ec The execution context in which to execute the queries.
      * @param diffConfig An implicit diff configuration.
      * @return An ordered list of dependent queries through [[QueryCollection[Seq]] that
      *         will resolve all conflicts with the schema in the database.
      */
    def migrateAsync()(
      implicit session: Session,
      space: KeySpace,
      helper: DatabaseHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): Future[Seq[ResultSet]] = {
      automigrate() fold(
        nel => Future.failed[Seq[ResultSet]](new Exception(s"Found ${nel.size} diff conflicts, unable to automatically migrate database")),
        success => executeStatements(success.diffs).sequence()
      )
    }


    def migrate(timeout: Duration = defaultDuration)(
      implicit session: Session,
      space: KeySpace,
      helper: DatabaseHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): Seq[ResultSet] = Await.result(migrateAsync(), timeout)
  }
}
