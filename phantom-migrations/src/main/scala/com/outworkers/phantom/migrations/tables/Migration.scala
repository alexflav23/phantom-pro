/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.{Session, TableMetadata}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.migrations.MigrationResult
import com.outworkers.phantom.migrations.diffs.{ColumnDiff, Diff, DiffConfig}

sealed case class Migration(
  additions: Diff,
  deletions: Diff,
  secondaryIndexes: Seq[CQLQuery]
) {

  def additiveQueries(table: CassandraTable[_, _])(
    implicit session: Session,
    keySpace: KeySpace
  ): Seq[CQLQuery] = {
    additions.columns map { col: ColumnDiff =>
      table.alter.add(col.name, col.cassandraType).qb
    }
  }

  def subtractionQueries(table: CassandraTable[_, _])(
    implicit keySpace: KeySpace
  ): Seq[CQLQuery] = {
    deletions.columns map { col =>
      table.alter.drop(col.name).qb
    }
  }

  def queryList(table: CassandraTable[_, _])(
    implicit session: Session,
    keySpace: KeySpace
  ): Seq[CQLQuery] = {
    additiveQueries(table) ++ subtractionQueries(table) ++ secondaryIndexes
  }

  def automigrate(table: CassandraTable[_, _])(
    implicit session: Session,
    keySpace: KeySpace
  ): QueryCollection[Seq] = {
    new QueryCollection(queryList(table).map(ExecutableCqlQuery(_, QueryOptions.empty, Nil)))
  }
}

object Migration {

  /**
    * Builds a migration for an existing table within a phantom database
    * against the same version of the table within Cassandra.
    * @param metadata The metadata loader from the cluster, which interrogates the Cassandra system tables.
    * @param table The phantom DSL Cassandra Table being migrated.
    * @param diffConfig The configuration and rules by which to execute the diff.
    * @return A [[cats.data.ValidatedNel]] of the [[Migration]], valid if no migration/diff rules
    *         have been breached according to the config.
    */
  def apply(
    metadata: TableMetadata,
    table: CassandraTable[_, _]
  )(implicit diffConfig: DiffConfig): MigrationResult[Migration] = {

    val dbTable = Diff(metadata)
    val phantomTable = Diff(table)

    implicit val space = KeySpace(metadata.getKeyspace.getName)

    import cats.implicits._

    (
      phantomTable diff dbTable migrations,
      phantomTable notIn dbTable migrations
    ).mapN { (additions: Diff, deletions: Diff) => Migration(additions, deletions, additions.indexes) }
  }

  def diff(
    first: CassandraTable[_, _],
    second: CassandraTable[_, _]
  )(implicit diffConfig: DiffConfig): MigrationResult[Migration] = {

    val firstDiff = Diff(first)
    val secondDiff = Diff(second)
    import cats.implicits._

    (
      firstDiff diff secondDiff migrations,
      firstDiff notIn secondDiff migrations
    ).mapN { (additions: Diff, deletions: Diff) => Migration(additions, deletions, Seq.empty) }
  }
}
