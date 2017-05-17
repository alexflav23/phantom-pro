/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.query.ExecutableStatementList
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl.Table
import com.outworkers.phantom.migrations.tables.Differ

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

package object migrations {

  val defaultDuration = 20.seconds

  implicit class TableMigrations(val table: Table[_, _]) extends AnyVal {

    def automigrate()(
      implicit session: Session,
      keySpace: KeySpace,
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): ExecutableStatementList[Seq] = {
      Differ.automigrate(table)
    }

    def automigrate(diffConfig: DiffConfig)(
      implicit session: Session,
      space: KeySpace,
      ec: ExecutionContextExecutor
    ): ExecutableStatementList[Seq] = {
      Differ.automigrate(table)(session, space, ec, diffConfig)
    }
  }

  implicit class DatabaseMigrations[DB <: Database[DB]](val db: DB) extends AnyVal {

    /**
      * Produces a final list of queries in the correct order than will need to be executed to resolve
      * all database diffs.
      * @param session The session in which to execute this operation.
      * @param space The keyspace in which the migration queries would execute.
      * @param ec The execution context in which to execute the queries.
      * @param diffConfig An implicit diff configuration.
      * @return An ordered list of dependent queries through [[ExecutableStatementList[Seq]] that
      *         will resolve all conflicts with the schema in the database.
      */
    def automigrate()(
      implicit session: Session,
      space: KeySpace,
      helper: MigrationHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): ExecutableStatementList[Seq] = helper.migrations(db)

    /**
      * Asynchronously executes all migration queries in a one by one fashion.
      * @param session The session in which to execute this operation.
      * @param space The keyspace in which the migration queries would execute.
      * @param ec The execution context in which to execute the queries.
      * @param diffConfig An implicit diff configuration.
      * @return An ordered list of dependent queries through [[ExecutableStatementList[Seq]] that
      *         will resolve all conflicts with the schema in the database.
      */
    def migrateAsync()(
      implicit session: Session,
      space: KeySpace,
      helper: MigrationHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): Future[Seq[ResultSet]] = automigrate().sequentialFuture()

    def migrate(timeout: Duration = defaultDuration)(
      implicit session: Session,
      space: KeySpace,
      helper: MigrationHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): Seq[ResultSet] = Await.result(migrateAsync(), timeout)

    def migrateToKeySpace(timeout: Duration = defaultDuration)(
      implicit session: Session,
      space: KeySpace,
      helper: MigrationHelper[DB],
      ec: ExecutionContextExecutor,
      diffConfig: DiffConfig
    ): Seq[ResultSet] = Await.result(migrateAsync(), timeout)
  }
}
