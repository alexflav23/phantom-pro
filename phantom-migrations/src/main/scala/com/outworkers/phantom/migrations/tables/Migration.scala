/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.tables

import cats.data.ValidatedNel
import com.datastax.driver.core.TableMetadata
import com.outworkers.phantom.connectors.KeySpace
import com.datastax.driver.core.Session
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.migrations.diffs.{ColumnDiff, Diff, DiffConfig, DiffConflict}

import scala.concurrent.ExecutionContext

sealed case class Migration(
  additions: Diff,
  deletions: Diff
) {

  def additiveQueries(table: CassandraTable[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Seq[CQLQuery] = {
    additions.columns map {
      col: ColumnDiff => table.alter.add(col.name, col.cassandraType).qb
    }
  }

  def subtractionQueries(table: CassandraTable[_, _])(
    implicit keySpace: KeySpace,
    ec: ExecutionContext
  ): Seq[CQLQuery] = {

    deletions.columns map { col =>
      table.alter.drop(col.name).qb
    }
  }

  def queryList(table: CassandraTable[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): Seq[CQLQuery] = {
    additiveQueries(table) ++ subtractionQueries(table)
  }

  def automigrate(table: CassandraTable[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext
  ): QueryCollection[Seq] = {
    new QueryCollection(queryList(table).map(ExecutableCqlQuery(_, QueryOptions.empty, Nil)))
  }
}

object Migration {
  def apply(
    metadata: TableMetadata,
    table: CassandraTable[_, _]
  )(implicit diffConfig: DiffConfig): ValidatedNel[DiffConflict, Migration] = {

    val dbTable = Diff(metadata)
    val phantomTable = Diff(table)

    import cats.implicits._

    (
      phantomTable diff dbTable migrations,
      phantomTable notIn dbTable migrations
    ).mapN { (additions: Diff, deletions: Diff) => Migration(additions, deletions) }
  }

  def diff(
    first: CassandraTable[_, _],
    second: CassandraTable[_, _]
  )(implicit diffConfig: DiffConfig): ValidatedNel[DiffConflict, Migration] = {
    val firstDiff = Diff(first)
    val secondDiff = Diff(second)
    import cats.implicits._

    (
      firstDiff diff secondDiff migrations,
      firstDiff notIn secondDiff migrations
    ).mapN { (additions: Diff, deletions: Diff) => Migration(additions, deletions) }
  }
}
