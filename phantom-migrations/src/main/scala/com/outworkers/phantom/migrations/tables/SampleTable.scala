package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.dsl._

abstract class SampleTable extends CassandraTable[SampleTable, SampleRecord] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object date extends DateTimeColumn(this)
}