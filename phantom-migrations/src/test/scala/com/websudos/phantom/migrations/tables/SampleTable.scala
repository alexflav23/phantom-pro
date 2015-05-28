package com.websudos.phantom.migrations.tables

import com.websudos.phantom.dsl._

case class SampleRecord(id: UUID, name: String, date: DateTime)

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