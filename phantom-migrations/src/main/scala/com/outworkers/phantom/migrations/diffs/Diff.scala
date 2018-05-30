/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.diffs

import com.datastax.driver.core.{ColumnMetadata, TableMetadata}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.column.OptionalColumn
import com.outworkers.phantom.connectors.KeySpace

import scala.collection.JavaConverters._
import cats.syntax.validated._
import cats.syntax.traverse._
import cats.instances.list._
import com.outworkers.phantom.migrations.MigrationResult

/**
  * The implementation of a migration diffing rule. We use this simple
  * function 1 API to navigate over a diff between two tables, which exposes
  * any columns that have been added or removed, or changed.
  *
  * We can then implement any logic on top of the diff between the tables,
  * and we can plug in the set of diffs and whichever stage of the migration pipeline,
  * meaning end users of the API are completely de-coupled from a static set of diffs.
  *
  * They can instead choose, mix and override the rules used for the final dif as they see fit.
  */
trait DiffRule extends (Diff => MigrationResult[List[ColumnDiff]])

object DiffRule {

  @deprecated("Do not rely on this as a default set of rules")
  def rules(
    diff: Diff
  )(implicit config: DiffConfig): MigrationResult[List[List[ColumnDiff]]] = {
    List(
      new EnforceOptionality(),
      new EnforceNoPrimaryOverrides()
    ).map(_.apply(diff)).sequence
  }

  class EnforceNoPrimaryOverrides()(
    implicit config: DiffConfig
  ) extends DiffRule {
    override def apply(v1: Diff): MigrationResult[List[ColumnDiff]] = {
      v1.columns.map { col =>
        if (col.isPrimary) {
          InvalidAddition(
            col.name,
            col.cassandraType,
            s"Cannot automatically migrate PRIMARY_KEY part ${col.name}. You cannot add a primary key to an existing table."
          ).invalidNel[ColumnDiff]
        } else {
          col.validNel[InvalidAddition]
        }
      } sequence
    }
  }

  class EnforceOptionality()(implicit config: DiffConfig) extends DiffRule {
    override def apply(v1: Diff): MigrationResult[List[ColumnDiff]] = {
      v1.columns.map { col =>
        if (!col.isOptional && !config.allowNonOptional) {
          InvalidAddition(
            col.name,
            col.cassandraType,
            s"You are trying to add a non-optional column to an existing schema. This means querying will now fail because previously inserted rows will not have ${col.name} as a property."
          ).invalidNel[ColumnDiff]
        } else {
          col.validNel[InvalidAddition]
        }
      } sequence
    }
  }
}

sealed case class Diff(columns: List[ColumnDiff], table: String, config: DiffConfig) {

  final def notIn(other: Diff): Diff = {
    Diff(
      columns = other.columns.filterNot(item => columns.exists(c => Comparison.NameComparison(c, item))),
      table = table,
      config = config
    )
  }


  final def diff(other: Diff): Diff = {
    Diff(
      columns.filterNot(item => other.columns.exists(c => Comparison.NameComparison(c, item))),
      s"$table - ${other.table}",
      config
    )
  }

  def hasPrimaryPart: Boolean = {
    columns.exists(_.isPrimary)
  }

  def indexes()(implicit keySpace: KeySpace): Seq[CQLQuery] = {
    columns.filter(_.isSecondary).map { origin =>
      QueryBuilder.Create.index(keySpace.name, table, origin.name)
    }
  }

  def migrations(implicit conf: DiffConfig): MigrationResult[Diff] = {
    DiffRule.rules(this).map( _=> this)
  }
}


trait Comparison extends ((ColumnDiff, ColumnDiff) => Boolean)

object Comparison {
  object NameComparison extends Comparison {
    val regex = "['\"]"

    val normalize: String => String = s => s.trim.replaceAll(regex, "").toLowerCase

    override def apply(v1: ColumnDiff, v2: ColumnDiff): Boolean = {
      normalize(v1.name) == normalize(v2.name)
    }
  }
}

object Diff {

  private[this] def contains(column: ColumnMetadata, clustering: List[String]): Boolean = {
    clustering.exists(column.getName ==)
  }

  def apply(metadata: TableMetadata)(implicit config: DiffConfig): Diff = {

    val primary = metadata.getPrimaryKey.asScala.map(_.getName).toList

    val columns = metadata.getColumns.asScala.toSet.foldLeft(List.empty[ColumnDiff])((acc, item) => {

      val sourceName = if (config.enableCaseSensitiveAutoQuotes) {
        if (item.getName.forall(_.isLower)) {
          item.getName
        } else {
          s""""${item.getName}"""".stripMargin
        }
      } else {
        item.getName
      }

      Console.println(s"Cassandra column name: $sourceName")

      acc :+ ColumnDiff(
        sourceName,
        cassandraType = item.getType.getName.toString,
        isOptional = false,
        isPrimary = contains(item, primary),
        isSecondary = item.isStatic,
        isStatic = item.isStatic
      )
    })

    Diff(columns, metadata.getName, config)
  }

  def apply(table: CassandraTable[_, _])(implicit config: DiffConfig): Diff = {
    val cols = table.columns.toList.map { column =>
      ColumnDiff(
        column.name,
        column.cassandraType,
        column.isInstanceOf[OptionalColumn[_, _, _]],
        column.isClusteringKey || column.isPartitionKey || column.isPrimary,
        column.isSecondaryKey,
        column.isStaticColumn
      )
    }
    Diff(cols, table.tableName, config)
  }
}
