package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._

case class SampleRecord(
  id: UUID,
  name: String,
  date: DateTime
)

abstract class SampleTable extends Table[SampleTable, SampleRecord] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object date extends DateTimeColumn(this)
}

abstract class SampleTableOneDiff extends Table[SampleTableOneDiff, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object name2 extends StringColumn(this)
  object date extends DateTimeColumn(this)
}

abstract class SampleTablePrimaryDiff extends Table[SampleTablePrimaryDiff, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object name extends StringColumn(this)
  object date extends DateTimeColumn(this)
}

class MigrationDatabase(override val connector: CassandraConnection) extends Database[MigrationDatabase](connector) {
  object sampleTable extends SampleTable with Connector
  object sampleTableOneDiff extends SampleTableOneDiff with Connector
  object sampleTablePrimaryDiff extends SampleTablePrimaryDiff with Connector
}


object MigrationDatabase extends MigrationDatabase(Defaults.connector)

trait MigrationDbProvider extends DatabaseProvider[MigrationDatabase] {
  override def database: MigrationDatabase = MigrationDatabase
}
