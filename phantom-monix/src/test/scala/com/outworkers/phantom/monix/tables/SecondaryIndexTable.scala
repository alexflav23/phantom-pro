package com.outworkers.phantom.monix.tables

import com.outworkers.phantom.monix._

case class SecondaryIndexRecord(
  primary: UUID,
  secondary: UUID,
  name: String
)

abstract class SecondaryIndexTable extends Table[
  SecondaryIndexTable,
  SecondaryIndexRecord
  ] {
  object id extends UUIDColumn with PartitionKey
  object secondary extends UUIDColumn with Index
  object name extends StringColumn
}
