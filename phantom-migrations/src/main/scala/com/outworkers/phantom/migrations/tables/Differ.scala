package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.{Session, TableMetadata}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.{CQLQuery, ExecutableStatementList}
import com.outworkers.phantom.connectors.KeySpace

import scala.concurrent._

private[phantom] object Differ {

  def metadata(tableName: String)(implicit session: Session, keySpace: KeySpace): TableMetadata = {
    blocking {
      session.getCluster.getMetadata.getKeyspace(keySpace.name).getTable(tableName)
    }
  }

  def queryList(table: CassandraTable[_, _])(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext, diffConfig: DiffConfig): Set[CQLQuery] = {
    Migration(metadata(table.tableName), table).queryList(table)
  }

  def automigrate(table: CassandraTable[_, _])(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext, diffConfig: DiffConfig): ExecutableStatementList = {
    new ExecutableStatementList(queryList(table))
  }
}