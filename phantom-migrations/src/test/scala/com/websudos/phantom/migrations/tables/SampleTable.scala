package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.dsl._



sealed abstract class SampleTable extends CassandraTable[SampleTable, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object date extends DateTimeColumn(this)

  def fromRow(row: Row): SampleRecord = {
    SampleRecord(
      id(row),
      name(row),
      date(row)
    )
  }
}

object SampleTable extends SampleTable with Connector


sealed abstract class SampleTableOneDiff extends CassandraTable[SampleTable, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object name2 extends StringColumn(this)
  object date extends DateTimeColumn(this)

  def fromRow(row: Row): SampleRecord = {
    SampleRecord(
      id(row),
      name(row),
      date(row)
    )
  }
}

object SampleTableOneDiff extends SampleTableOneDiff with Connector


sealed abstract class SampleTablePrimaryDiff extends CassandraTable[SampleTable, SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)
  object date extends DateTimeColumn(this)

  def fromRow(row: Row): SampleRecord = {
    SampleRecord(
      id(row),
      name(row),
      date(row)
    )
  }
}

object SampleTablePrimaryDiff extends SampleTablePrimaryDiff with Connector