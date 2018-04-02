/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.diffs

import cats.Traverse
import cats.data.Validated.Valid
import cats.data.{NonEmptyList, ValidatedNel}
import com.datastax.driver.core.{ColumnMetadata, TableMetadata}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.column.OptionalColumn
import com.outworkers.phantom.connectors.KeySpace

import scala.collection.JavaConverters._

sealed case class Diff(columns: List[ColumnDiff], table: String, config: DiffConfig) {

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

  protected[phantom] def enforceOptionality() = {
    //import cats.instances.all._
    import cats.implicits._

    val res = columns.map { col =>
      if (!col.isOptional && !config.allowNonOptional) {
        InvalidAddition(
          col.name,
          col.cassandraType,
          s"You are trying to add a non-optional column to an existing schema. This means querying will now fail because previously inserted rows will not have ${col.name} as a property."
        ).invalidNel
      } else {
        col.validNel
      }
    }


    res.sequence_
  }

  protected[phantom] def enforceNoPrimaryOverrides(): Unit = {
    columns.foreach { col =>
      if (col.isPrimary) {
        throw new Exception(s"Cannot automatically migrate PRIMARY_KEY part ${col.name}. You cannot add a primary key to an existing table.")
      }
    }
  }

  def migrations(): Seq[ColumnDiff] = {
    enforceOptionality()
    enforceNoPrimaryOverrides()
    columns
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
      acc :+ ColumnDiff(
        item.getName,
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
