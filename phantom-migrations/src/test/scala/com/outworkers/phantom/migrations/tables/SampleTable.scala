package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.dsl.{context => _, _}

case class SampleRecord(
  id: UUID,
  name: String,
  date: DateTime
)

abstract class SampleTable extends Table[SampleTable, SampleRecord] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object date extends DateTimeColumn
}

abstract class SampleTableOneDiff extends Table[SampleTableOneDiff, SampleRecord] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object name2 extends StringColumn
  object date extends DateTimeColumn
}

abstract class SampleTablePrimaryDiff extends Table[SampleTablePrimaryDiff, SampleRecord] {
  object id extends UUIDColumn with PartitionKey
  object id2 extends UUIDColumn with PrimaryKey
  object name extends StringColumn
  object date extends DateTimeColumn
}

