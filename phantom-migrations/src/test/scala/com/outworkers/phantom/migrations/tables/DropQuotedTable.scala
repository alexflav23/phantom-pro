package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.dsl.{PartitionKey, RootConnector, Table}


abstract class DropQuotedTable extends Table[DropQuotedTable, SampleRecord] {
  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn {
    override def name: String = """"this_is_a_source_name""""
  }

  object date extends DateTimeColumn
}

abstract class DroppedQuotedTable extends Table[DroppedQuotedTable, SampleRecord] {
  object id extends UUIDColumn with PartitionKey
  object date extends DateTimeColumn
}