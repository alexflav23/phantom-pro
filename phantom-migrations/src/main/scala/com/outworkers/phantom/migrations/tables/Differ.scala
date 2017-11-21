/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.tables

import com.datastax.driver.core.{Session, TableMetadata}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.dsl.Table
import com.outworkers.phantom.migrations.DiffConfig

import scala.concurrent._

private[phantom] object Differ {

  def metadata(tableName: String)(implicit session: Session, keySpace: KeySpace): TableMetadata = {
    blocking {
      session.getCluster.getMetadata.getKeyspace(keySpace.name).getTable(tableName)
    }
  }

  def queryList(table: Table[_, _])(
    implicit session: Session,
    keySpace: KeySpace, ec: ExecutionContext, diffConfig: DiffConfig
  ): Seq[ExecutableCqlQuery] = {
    Migration(metadata(table.tableName), table).queryList(table).map(ExecutableCqlQuery(_))
  }

  def automigrate(table: Table[_, _])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContext,
    diffConfig: DiffConfig
  ): QueryCollection[Seq] = {
    new QueryCollection(queryList(table))
  }
}
