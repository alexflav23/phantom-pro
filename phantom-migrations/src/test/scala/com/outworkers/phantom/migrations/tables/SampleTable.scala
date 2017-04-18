package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._


abstract class SampleTableOneDiff extends CassandraTable[SampleTable, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object name2 extends StringColumn(this)
  object date extends DateTimeColumn(this)
}

abstract class SampleTablePrimaryDiff extends CassandraTable[SampleTable, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object name extends StringColumn(this)
  object date extends DateTimeColumn(this)
}

class MigrationDatabase(override val connector: CassandraConnection) extends Database[MigrationDatabase](connector) {
  object sampleTableOneDiff extends SampleTableOneDiff with Connector
  object sampleTablePrimaryDiff extends SampleTablePrimaryDiff with Connector
}


object MigrationDatabase extends MigrationDatabase(ContactPoint.local.keySpace("phantom_pro"))

trait MigrationDbProvider extends DatabaseProvider[MigrationDatabase] {
  override def database: MigrationDatabase = MigrationDatabase
}
