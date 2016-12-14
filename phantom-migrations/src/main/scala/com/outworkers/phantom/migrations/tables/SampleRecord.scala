package com.outworkers.phantom.migrations.tables

import com.outworkers.phantom.dsl._

case class SampleRecord(
  id: UUID,
  name: String,
  date: DateTime
)
