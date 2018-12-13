package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.dsl._

case class MigrationRecord(
  id: Int,
  productId: Int,
  name: String,
  description: Option[String]
)

abstract class MissingColumnNameTable extends Table[MissingColumnNameTable, MigrationRecord] {
  object id extends IntColumn with PartitionKey
  object productId extends IntColumn  with PartitionKey
  object name extends StringColumn
  object description extends OptionalStringColumn
}


abstract class MissingAddedColumnNameTable extends Table[MissingAddedColumnNameTable, MigrationRecord] {
  object id extends IntColumn with PartitionKey
  object productId extends IntColumn  with PartitionKey
  object name extends StringColumn
  object description extends OptionalStringColumn
  object executionassignments extends SetColumn[Int] with Index
  object teststepassignments extends SetColumn[Int]
}
