package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.{ColumnMetadata, TableMetadata}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.dsl.OptionalColumn

import scala.collection.JavaConverters._

sealed case class DiffConfig(
  allowNonOptional: Boolean,
  allowSecondaryOverwrites: Boolean
)

sealed case class Diff(columns: Set[ColumnDiff], table: String, config: DiffConfig) {

  final def diff(other: Diff): Diff = {
    Diff(
      columns.filterNot(item => other.columns.exists(_.name == item.name)),
      s"$table - ${other.table}", config
    )
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

  protected[phantom] def enforceOptionality() = {
    columns.foreach {
      col => {
        if (!col.isOptional && !config.allowNonOptional) {
          throw new Exception(s"You are trying to add a non-optional column to an existing schema. This means querying will now fail because previously inserted rows will not have ${col.name} as a property. ")
        }
      }
    }
  }

  protected[phantom] def enforceNoPrimaryOverrides() = {
    columns.foreach {
      col => {
        if (col.isPrimary) {
          throw new Exception(s"Cannot automatically migrate PRIMARY_KEY part ${col.name}. You cannot add a primary key to an existing table.")
        } else {
          true
        }
      }
    }
  }

  def migrations(): Set[ColumnDiff] = {
    enforceOptionality()
    enforceNoPrimaryOverrides()
    columns
  }

}

object Diff {

  private[this] def contains(column: ColumnMetadata, clustering: List[String]): Boolean = {
    clustering.exists(column.getName ==)
  }

  def apply(metadata: TableMetadata)(implicit config: DiffConfig): Diff = {

    val primary = metadata.getPrimaryKey.asScala.map(_.getName).toList

    val columns = metadata.getColumns.asScala.toSet.foldLeft(Set.empty[ColumnDiff])((acc, item) => {
      acc + ColumnDiff(
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
    val cols = table.columns.toSet[AbstractColumn[_]].map {
      column => {
        ColumnDiff(
          column.name,
          column.cassandraType,
          column.isInstanceOf[OptionalColumn[_, _, _]],
          column.isClusteringKey || column.isPartitionKey || column.isPrimary,
          column.isSecondaryKey,
          column.isStaticColumn
        )
      }
    }
    Diff(cols, table.tableName, config)
  }
}
