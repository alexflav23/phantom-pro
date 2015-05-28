package com.websudos.phantom.migrations

import com.websudos.phantom.builder.query.CQLQuery

import scala.collection.JavaConverters._
import com.datastax.driver.core.{TableMetadata, ColumnMetadata}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace

sealed case class Diff(columns: Set[ColumnDiff], table: String) {

  final def diff(other: Diff): Diff = {
    Diff(columns.filterNot(item => other.columns.exists(_.name == item.name)), s"$table - ${other.table}")
  }

  def hasPrimaryPart: Boolean = {
    columns.exists(_.isPrimary)
  }

  def indexes()(implicit keySpace: KeySpace): Set[CQLQuery] = {
    columns.filter(_.isSecondary).map {
      origin => {
        CQLQuery(s"CREATE INDEX IF NOT EXISTS ${origin.name} on ${QueryBuilder.keyspace(keySpace.name, table).queryString}")
      }
    }
  }

  def migrations(): Set[ColumnDiff] = {
    columns.filter {
      col => {
        if (col.isPrimary) {
          throw new Exception(s"Cannot automatically migrate PRIMARY_KEY part ${col.name}")
        } else {
          true
        }
      }
    }
  }

}

object Diff {

  private[this] def contains(column: ColumnMetadata, clustering: List[String]): Boolean = {
    clustering.exists(column.getName ==)
  }

  def apply(metadata: TableMetadata): Diff = {

    val primary = metadata.getPrimaryKey.asScala.map(_.getName).toList

    val columns = metadata.getColumns.asScala.toSet.foldLeft(Set.empty[ColumnDiff])((acc, item) => {
      acc + ColumnDiff(
        item.getName,
        item.getType.getName.toString,
        contains(item, primary),
        item.isStatic,
        item.isStatic
      )
    })

    Diff(columns, metadata.getName)
  }

  def apply(table: CassandraTable[_, _]): Diff = {
    val cols = table.columns.toSet[AbstractColumn[_]].map {
      column => {
        ColumnDiff(
          column.name,
          column.cassandraType,
          column.isClusteringKey,
          column.isSecondaryKey,
          column.isStaticColumn
        )
      }
    }
    Diff(cols, table.tableName)
  }
}
