package com.outworkers.phantom.migrations.diffs

case class ColumnDiff(
  name: String,
  cassandraType: String,
  isOptional: Boolean,
  isPrimary: Boolean,
  isSecondary: Boolean,
  isStatic: Boolean
)
