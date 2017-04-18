package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.{Session, TableMetadata}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.builder.query.ExecutableStatementList
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.concurrent._

private[phantom] object Differ {

  def metadata(tableName: String)(implicit session: Session, keySpace: KeySpace): TableMetadata = {
    blocking {
      session.getCluster.getMetadata.getKeyspace(keySpace.name).getTable(tableName)
    }
  }

  def queryList[T <: CassandraTable[T, R], R](table: T)(
    implicit session: Session,
    keySpace: KeySpace, ec: ExecutionContext, diffConfig: DiffConfig
  ): Set[CQLQuery] = {
    Migration(metadata(table.tableName), table).queryList(table)
  }

  def automigrate[T <: CassandraTable[T, R], R](table: T)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext,
    diffConfig: DiffConfig
  ): ExecutableStatementList[Set] = {
    new ExecutableStatementList(queryList[T, R](table))
  }
}
