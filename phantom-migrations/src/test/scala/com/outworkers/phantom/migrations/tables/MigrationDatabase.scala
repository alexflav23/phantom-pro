/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl.{Database, DatabaseProvider}

class MigrationDatabase(
  override val connector: CassandraConnection
) extends Database[MigrationDatabase](connector) {

  object dropQuotedTable extends DropQuotedTable with Connector
  object droppedQuotedTable extends DroppedQuotedTable with Connector

  object sampleTable extends SampleTable with Connector
  object sampleTableOneDiff extends SampleTableOneDiff with Connector
  object sampleTablePrimaryDiff extends SampleTablePrimaryDiff with Connector

  object missingColumnTable extends MissingColumnNameTable with Connector
  object missingColumnTableAdded extends MissingAddedColumnNameTable with Connector
}

object MigrationDatabase extends MigrationDatabase(Defaults.connector)

trait MigrationDbProvider extends DatabaseProvider[MigrationDatabase] {
  override def database: MigrationDatabase = MigrationDatabase
}
