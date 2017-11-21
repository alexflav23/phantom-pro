/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.TableMetadata
import com.outworkers.phantom.connectors.KeySpace
import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.dsl.Table
import com.outworkers.phantom.migrations.DiffConfig

import scala.concurrent.ExecutionContext

case class ColumnDiff(
  name: String,
  cassandraType: String,
  isOptional: Boolean,
  isPrimary: Boolean,
  isSecondary: Boolean,
  isStatic: Boolean
)

sealed case class Migration(
  additions: Seq[ColumnDiff],
  deletions: Seq[ColumnDiff]
) {

  def additiveQueries(table: Table[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Seq[CQLQuery] = {
    additions map {
      col: ColumnDiff => table.alter.add(col.name, col.cassandraType).qb
    }
  }

  def subtractionQueries(table: Table[_, _])(
    implicit keySpace: KeySpace,
    ec: ExecutionContext
  ): Seq[CQLQuery] = {
    deletions map { col => table.alter.drop(col.name).qb }
  }

  def queryList(table: Table[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Seq[CQLQuery] = {
    additiveQueries(table) ++ subtractionQueries(table)
  }

  def automigrate(table: Table[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): QueryCollection[Seq] = {
    new QueryCollection(queryList(table).map(ExecutableCqlQuery(_)))
  }
}

object Migration {
  def apply(metadata: TableMetadata, table: Table[_, _])(implicit diffConfig: DiffConfig): Migration = {

    val dbTable = Diff(metadata)
    val phantomTable = Diff(table)

    Migration(
      phantomTable diff dbTable migrations(),
      dbTable diff phantomTable migrations()
    )
  }

  def diff(first: Table[_, _], second: Table[_, _])(implicit diffConfig: DiffConfig): Migration = {
    val firstDiff = Diff(first)
    val secondDiff = Diff(second)

    Migration(
      firstDiff diff secondDiff migrations(),
      secondDiff diff firstDiff migrations()
    )
  }
}
