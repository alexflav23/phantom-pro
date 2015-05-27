package com.websudos.phantom.migrations

import com.datastax.driver.core.{Session, TableMetadata}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.{ExecutableStatementList, CQLQuery}
import com.websudos.phantom.connectors.KeySpace

import scala.concurrent._

private[phantom] object Differ {

  def metadata(tableName: String)(implicit session: Session, keySpace: KeySpace): TableMetadata = {
    blocking {
      session.getCluster.getMetadata.getKeyspace(keySpace.name).getTable(tableName)
    }
  }

  def queryList(table: CassandraTable[_, _])(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext): Set[CQLQuery] = {
    Migration(metadata(table.tableName), table).queryList(table)
  }

  def automigrate(table: CassandraTable[_, _])(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext): ExecutableStatementList = {
    new ExecutableStatementList(queryList(table))
  }
}